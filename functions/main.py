from firebase_functions import https_fn
from firebase_admin import firestore, initialize_app
from openai import OpenAI
from dotenv import load_dotenv
from firebase_admin import auth
import os
from datetime import datetime, timedelta
import re

load_dotenv()
initialize_app()
db = firestore.client()
client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

# 사용자 메시지에서 예약 정보 추출
def parse_input(text, userID):
    room_match = re.search(r'(\d{4})호', text)
    date_match = re.search(r'(\d{1,2})월\s*(\d{1,2})일', text)
    time_match = re.search(r'(오전|오후)?\s*(\d{1,2})시', text)
    duration_match = re.search(r'(\d+)시간', text)
    event_name_match = re.search(r'동안\s*(.+?)\s*예약', text)

    if not (room_match and date_match and time_match and duration_match):
        return None

    roomID = room_match.group(1)
    month, day = map(int, date_match.groups())
    ampm, hour = time_match.groups()
    hour = int(hour)
    if ampm == "오후" and hour < 12:
        hour += 12
    elif ampm == "오전" and hour == 12:
        hour = 0
    duration = int(duration_match.group(1))
    event_name = event_name_match.group(1) if event_name_match else "예약"

    now = datetime.now()
    start = datetime(now.year, month, day, hour)
    end = start + timedelta(hours=duration)

    return {
        "roomID": roomID,
        "startTime": start,
        "endTime": end,
        "eventName": event_name,
        "status": "확정",
        "userID": userID
    }

@https_fn.on_request()
def chat_or_reserve(req: https_fn.Request) -> https_fn.Response:
    try:
        data = req.get_json()
        user_input = data.get("message", "")
        
        id_token = req.headers.get("Authorization", "").replace("Bearer ", "")
        userID = "unknown"
        if id_token:
            try:
                decoded_token = auth.verify_id_token(id_token)
                userID = decoded_token.get("uid", "unknown")
            except Exception as e:
                return https_fn.Response("유효하지 않은 토큰입니다.", status=401)

        
        room_match = re.search(r'(\d{4})호', user_input)
        if room_match:
            roomID = room_match.group(1)
            room_doc = db.collection("rooms").document(roomID).get()

            if room_doc.exists:
                room_data = room_doc.to_dict()
                capacity = room_data.get("capacity", "정보 없음")
                location = room_data.get("location", "위치 정보 없음")
                equipment = room_data.get("equipment", [])

                
                known_equipment = ["마이크", "빔프로젝터", "스피커", "화이트보드"]
                for eq in known_equipment:
                    if eq in user_input:
                        if eq in equipment:
                            return https_fn.Response(f"{roomID}호 강의실에는 '{eq}'이(가) 있습니다.", status=200)
                        else:
                            return https_fn.Response(f"{roomID}호 강의실에는 '{eq}'이(가) 없습니다.", status=200)

                
                if "수용" in user_input or "몇 명" in user_input:
                    return https_fn.Response(f"{roomID}호 강의실의 수용 인원은 {capacity}명입니다.", status=200)

                
                if "위치" in user_input or "어디" in user_input:
                    return https_fn.Response(f"{roomID}호 강의실의 위치는 '{location}'입니다.", status=200)

                
                equipment_str = ", ".join(equipment) if equipment else "없음"
                info = f"{roomID}호 강의실 정보:\n- 위치: {location}\n- 수용 인원: {capacity}명\n- 기자재: {equipment_str}"
                return https_fn.Response(info, status=200)

            else:
                return https_fn.Response(f"{roomID}호 강의실 정보를 찾을 수 없습니다.", status=404)

        
        gpt_response = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {
                    "role": "system",
                    "content": "너는 강의실 예약을 처리하는 비서야. 사용자의 메시지가 예약 요청이면 날짜/시간/강의실을 포함해 JSON 형태로 응답해. 예약 요청이 아니면 'null'이라고 대답해."
                },
                {"role": "user", "content": user_input}
            ]
        )

        ai_content = gpt_response.choices[0].message.content.strip()

        
        if "호" in ai_content and "예약" in ai_content:
            parsed = parse_input(user_input, userID)
            if not parsed:
                return https_fn.Response("예약 요청으로 보이지만 날짜/시간 파싱에 실패했습니다.", status=400)

            
            conflict = db.collection("Reservations") \
                .where("roomID", "==", parsed["roomID"]) \
                .where("startTime", "<", parsed["endTime"].isoformat()) \
                .where("endTime", ">", parsed["startTime"].isoformat()) \
                .get()

            if conflict:
                return https_fn.Response("이미 예약된 시간입니다.", status=409)

            db.collection("Reservations").add(parsed)
            return https_fn.Response(f"{parsed['roomID']} 강의실이 예약되었습니다.", status=200)

        
        general_reply = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": "너는 친절하고 유익한 챗봇이야."},
                {"role": "user", "content": user_input}
            ]
        )
        reply = general_reply.choices[0].message.content
        return https_fn.Response(reply, status=200)

    except Exception as e:
        return https_fn.Response(f"Error: {str(e)}", status=500)
