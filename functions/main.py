from firebase_functions import https_fn
from firebase_admin import firestore, initialize_app, auth, credentials
from openai import OpenAI
from dotenv import load_dotenv
from datetime import datetime, timedelta
from pathlib import Path
import os
import json
import re
import logging

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

@https_fn.on_request()
def ai_assistant(req: https_fn.Request) -> https_fn.Response:
    try:
        data = req.get_json()
        user_input = data.get("message", "")

        id_token = req.headers.get("Authorization", "").replace("Bearer ", "")
        userID = "unknown"
        if id_token:
            try:
                decoded_token = auth.verify_id_token(id_token)
                userID = decoded_token.get("uid", "unknown")
            except:
                return https_fn.Response("유효하지 않은 토큰입니다.", status=401)

        system_prompt = f"""너는 Firestore 기반 강의실 예약 도우미야. 사용자의 한국어 문장을 먼저 자연스럽게 오타 없이 교정하고, 그 다음 아래 JSON 명령 중 하나로 변환해.

반드시 아래 형식을 따르고, **JSON만 반환**해야 해.
설명, 문장, 주석 등은 출력하지 마. 오직 JSON 한 개만 반환해.

---

###명령 유형과 JSON 구조

1. 기자재 확인
{{
"action": "query_equipment",
"room": "5104",
"item": "TV"
}}

2. 강의실 예약
{{
"action": "reserve",
"room": "5104",
"startTime": "2025-05-21T13:00:00",
"duration": 2,
"eventName": "스터디",
"userID": "{userID}"
}}

3. 예약 취소
{{
"action": "cancel_reservation",
"room": "5104",
"userID": "{userID}"
}}

4. 최신 공지
{{ "action": "latest_notice" }}

5. 내가 쓴 리뷰
{{
"action": "my_reviews",
"userID": "{userID}"
}}

6. 강의실 추천
{{
"action": "recommend_room",
"keywords": ["6명", "TV", "마이크"]
}}

7. 강의실 평가 요청
{{
"action": "review_summary",
"room": "5104"
}}

---

###  recommend_room 의 keywords 규칙

- 무조건 문자열 리스트(List[str])로 작성
- 키워드는 다음 중 포함 가능:
- 인원수: `"6명"`, `"8명"` 등
- 기자재: `"TV"`, `"마이크"`, `"전자칠판"`, `"빔프로젝터"`
- 시간: `"지금"`

---

주의: 반드시 위 JSON 형식 중 하나만 반환해야 하며, 그 외의 자연어 설명은 출력하지 마."""

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
            logging.warning(f"GPT 응답 파싱 실패: {gpt_response_text}")
            return https_fn.Response("죄송해요, 질문을 이해하지 못했어요. 예: '5104호에 TV 있나요?'처럼 다시 질문해 주세요.", status=400)

        action = query.get("action")
        if not action:
            return https_fn.Response("죄송해요, 요청을 파악하지 못했어요. 다시 말씀해 주세요.", status=400)

        if "room" in query:
            query["room"] = extract_room_id(query["room"]) or query["room"]

        if action == "query_equipment":
            doc = db.collection("rooms").document(query["room"]).get()
            if not doc.exists:
                return https_fn.Response("강의실이 존재하지 않습니다.", status=404)
            eq = doc.to_dict().get("equipment", [])
            item = query.get("item")
            if not item or item in ["뭐", "무엇", "있는지", "있는가"]:
                eq_list = ", ".join(eq) if eq else "없음"
                return https_fn.Response(f"{query['room']}호에 있는 기자재: {eq_list}", status=200)
            return https_fn.Response(f"{query['room']}호에 '{item}'이(가) {'있습니다' if item in eq else '없습니다'}.", status=200)

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
                .where("startTime", ">", now).get()
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
            sorted_reviews = sorted(reviews, key=lambda x: x.get("createdAt", ""), reverse=True)
            preview = sorted_reviews[:2]
            reply_lines = [f"{r.get('roomID', '?')}호: {r.get('comment', '')} (★{r.get('rating', '?')})" for r in preview]
            reply_lines.append("\n자세한 리뷰는 마이페이지에서 확인해 주세요 😊")
            return https_fn.Response("\n\n".join(reply_lines), status=200)

        elif action == "recommend_room":
            keywords = query.get("keywords", [])
            person_count = next((int(k.replace("명", "")) for k in keywords if k.endswith("명") and k[:-1].isdigit()), None)
            matched = []
            for doc in db.collection("rooms").stream():
                data = doc.to_dict()
                eq = data.get("equipment", [])
                cap = data.get("capacity", 0)
                if person_count is not None and cap < person_count:
                    continue
                score = sum(1 for k in keywords if k in eq)
                if score > 0 or person_count is not None:
                    matched.append((score, doc.id, data))
            if not matched:
                return https_fn.Response("조건에 맞는 강의실이 없어요 😥", status=200)
            matched.sort(reverse=True)
            _, room_id, best = matched[0]
            location = best.get("location", "정보 없음")
            capacity = best.get("capacity", "정보 없음")
            equipment = ", ".join(best.get("equipment", [])) or "없음"
            reviews = db.collection("Reviews").where("roomID", "==", room_id).stream()
            ratings, pos_comments, neg_comments, latest_comment, latest_time = [], [], [], None, None
            for r in reviews:
                review = r.to_dict()
                rating = review.get("rating")
                comment = review.get("comment", "")
                created = review.get("createdAt")
                if rating is not None:
                    ratings.append(rating)
                    if rating >= 4: pos_comments.append(comment)
                    elif rating <= 2: neg_comments.append(comment)
                if created and comment and (not latest_time or str(created) > str(latest_time)):
                    latest_comment, latest_time = comment, created
            avg = round(sum(ratings)/len(ratings), 1) if ratings else None
            pos_rate = round(len(pos_comments)/len(ratings)*100) if ratings else 0
            neg_rate = 100 - pos_rate if ratings else 0
            response = f"""조건에 맞는 강의실을 찾아봤어요! 😊
📍 위치: {location}
🏫 강의실: {room_id}호 (최대 {capacity}명)
🛠️ 기자재: {equipment}"""
            if latest_comment:
                response += f'\n📝 최근 후기: "{latest_comment}"'
            if avg is not None:
                response += f"\n⭐ 평균 평점: {avg}점\n📊 긍정 {pos_rate}%, 부정 {neg_rate}%"
            response += "\n\n이 강의실로 예약해드릴까요?"
            return https_fn.Response(response, status=200)

        elif action == "review_summary":
            room = query["room"]
            docs = db.collection("Reviews").where("roomID", "==", room).stream()
            ratings, pos_comments, neg_comments = [], [], []
            for r in docs:
                review = r.to_dict()
                rating = review.get("rating")
                comment = review.get("comment", "")
                if rating is not None:
                    ratings.append(rating)
                    if rating >= 4:
                        pos_comments.append(comment)
                    elif rating <= 2:
                        neg_comments.append(comment)
            if not ratings:
                return https_fn.Response(f"{room}호에 등록된 후기가 없어요.", status=200)
            avg = round(sum(ratings) / len(ratings), 1)
            pos_ratio = round(len(pos_comments) / len(ratings) * 100)
            neg_ratio = 100 - pos_ratio
            pos_line = f'🟢 긍정 후기: "{pos_comments[-1]}"' if pos_comments else ""
            neg_line = f'🔴 부정 후기: "{neg_comments[-1]}"' if neg_comments else ""
            summary = f"""[{room}호 강의실 평가 요약]
{pos_line}
{neg_line}
⭐ 평균 평점: {avg}점
📊 긍정 {pos_ratio}%, 부정 {neg_ratio}%"""
            return https_fn.Response(summary, status=200)

        return https_fn.Response("죄송해요, 요청을 이해하지 못했어요 😥", status=400)

    except Exception as e:
        logging.exception("예외 발생:")
        return https_fn.Response("알 수 없는 오류가 발생했어요. 잠시 후 다시 시도해 주세요.", status=500)
