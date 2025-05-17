from firebase_functions import https_fn
from firebase_admin import firestore, initialize_app, auth, credentials
from openai import OpenAI
from dotenv import load_dotenv
from datetime import datetime, timedelta
from pathlib import Path
import os
import json
import re

# 환경 설정
load_dotenv()
BASE_DIR = Path(__file__).resolve().parent
cred = credentials.Certificate(BASE_DIR / "serviceAccountKey.json")
initialize_app(cred)

db = firestore.client()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

# 방 번호 추출
def extract_room_id(text):
    m = re.search(r'(\d)\s*(?:강의동|동)?\s*[-\s]?\s*(\d{2,3})\s*호?', text)
    if m:
        return m.group(1) + m.group(2)
    m2 = re.search(r'(\d{3,4})\s*호?', text)
    if m2:
        return m2.group(1)
    return None

# GPT로 오타 자동 교정
def correct_input_with_gpt(raw_text):
    try:
        response = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": "사용자의 한국어 문장을 자연스럽게 오타 없이 교정해줘."},
                {"role": "user", "content": raw_text}
            ]
        )
        return response.choices[0].message.content.strip()
    except Exception:
        return raw_text  # 실패 시 원본 반환

@https_fn.on_request()
def ai_assistant(req: https_fn.Request) -> https_fn.Response:
    try:
        data = req.get_json()
        user_input = data.get("message", "")
        user_input = correct_input_with_gpt(user_input)  # GPT로 오타 교정

        id_token = req.headers.get("Authorization", "").replace("Bearer ", "")
        userID = "unknown"

        if id_token:
            try:
                decoded_token = auth.verify_id_token(id_token)
                userID = decoded_token.get("uid", "unknown")
            except:
                return https_fn.Response("유효하지 않은 토큰입니다.", status=401)

        system_prompt = f"""
        너는 Firestore 기반 강의실 도우미야. 사용자의 자연어 입력을 다음 JSON 명령으로 변환해.
        - query_equipment: {{ "action": "query_equipment", "room": "5104", "item": "TV" }}
        - reserve: {{ "action": "reserve", "room": "5104", "startTime": "...", "duration": 2, "eventName": "...", "userID": "{userID}" }}
        - cancel_reservation: {{ "action": "cancel_reservation", "room": "5104", "userID": "{userID}" }}
        - latest_notice: {{ "action": "latest_notice" }}
        - my_reviews: {{ "action": "my_reviews", "userID": "{userID}" }}
        - recommend_room: {{ "action": "recommend_room", "keywords": ["TV", "마이크"] }}
        참고: rooms의 기자재 필드는 equiment라는 오타로 저장되어 있음.
        """.strip()

        gpt = client.chat.completions.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_input}
            ]
        )

        gpt_response_text = gpt.choices[0].message.content.strip()
        try:
            query = json.loads(gpt_response_text)
        except json.JSONDecodeError:
            return https_fn.Response("죄송해요, 질문을 이해하지 못했어요. 예: '5104호에 TV 있나요?'처럼 다시 질문해 주세요.", status=400)

        action = query.get("action")
        if not action:
            return https_fn.Response("죄송해요, 요청을 파악하지 못했어요. '5301호에 마이크 있나요?'처럼 다시 질문해 주세요.", status=400)

        if "room" in query:
            query["room"] = extract_room_id(query["room"]) or query["room"]

        if action == "query_equipment":
            doc = db.collection("rooms").document(query["room"]).get()
            if not doc.exists:
                return https_fn.Response("강의실이 존재하지 않습니다.", status=404)

            eq = doc.to_dict().get("equiment", [])
            item = query.get("item")

            if not item or item in ["뭐", "무엇", "있는지", "있는가"]:
                eq_list = ", ".join(eq) if eq else "없음"
                return https_fn.Response(f"{query['room']}호에 있는 기자재: {eq_list}", status=200)

            if item in eq:
                return https_fn.Response(f"{query['room']}호에 '{item}'이(가) 있습니다.", status=200)
            else:
                return https_fn.Response(f"{query['room']}호에는 '{item}'이(가) 없습니다.", status=200)

        elif action == "reserve":
            start = datetime.fromisoformat(query["startTime"])
            end = start + timedelta(hours=query["duration"])

            conflict = db.collection("Reservations") \
                .where("roomID", "==", query["room"]) \
                .where("startTime", "<", end.isoformat()) \
                .where("endTime", ">", start.isoformat()) \
                .get()

            if conflict:
                return https_fn.Response("이미 예약된 시간입니다.", status=409)

            db.collection("Reservations").add({
                "roomID": query["room"],
                "startTime": start,
                "endTime": end,
                "eventName": query.get("eventName", "일반 예약"),
                "status": "확정",
                "userID": query["userID"]
            })

            return https_fn.Response(f"{query['room']}호가 예약되었습니다.", status=200)

        elif action == "cancel_reservation":
            now = datetime.utcnow().isoformat()
            docs = db.collection("Reservations") \
                .where("roomID", "==", query["room"]) \
                .where("userID", "==", query["userID"]) \
                .where("startTime", ">", now) \
                .get()

            if not docs:
                return https_fn.Response("취소할 예약이 없습니다.", status=404)

            for doc in docs:
                db.collection("Reservations").document(doc.id).delete()

            return https_fn.Response(f"{query['room']}호 예약이 취소되었습니다.", status=200)

        elif action == "latest_notice":
            docs = db.collection("Notices").order_by("createdAt", direction=firestore.Query.DESCENDING).limit(1).get()
            if not docs:
                return https_fn.Response("공지사항이 없습니다.", status=200)
            notice = docs[0].to_dict()
            return https_fn.Response(f"[{notice['title']}]\n{notice['content']}", status=200)

        elif action == "my_reviews":
            docs = db.collection("Reviews").where("userID", "==", query["userID"]).stream()
            reviews = [d.to_dict() for d in docs]
            if not reviews:
                return https_fn.Response("작성한 리뷰가 없습니다.", status=200)
            reply = "\n\n".join(f"{r['roomID']}호: {r['comment']} (★{r['rating']})" for r in reviews)
            return https_fn.Response(reply, status=200)

        elif action == "recommend_room":
            keywords = query.get("keywords", [])
            matched = []
            docs = db.collection("rooms").stream()
            for doc in docs:
                data = doc.to_dict()
                eq = data.get("equiment", [])
                score = sum(1 for k in keywords if k in eq)
                if score > 0:
                    matched.append((score, doc.id, data))

            if not matched:
                return https_fn.Response("추천할 강의실이 없습니다.", status=200)

            matched.sort(reverse=True)
            best = matched[0][2]
            return https_fn.Response(
                f"{matched[0][1]}호 추천: 위치 {best['location']}, 수용 {best['capacity']}명, 기자재: {', '.join(best['equiment'])}",
                status=200
            )

        return https_fn.Response("요청을 이해할 수 없습니다.", status=400)

    except Exception as e:
        return https_fn.Response(f"오류 발생: {str(e)}", status=500)
