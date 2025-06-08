from firebase_functions import https_fn
from firebase_admin import firestore, initialize_app, auth, credentials
from openai import OpenAI
from dotenv import load_dotenv
from datetime import datetime, timedelta, timezone
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

KST = timezone(timedelta(hours=9))

# 방 번호 추출 함수
def extract_room_id(text):
    m = re.search(r'(\d)\s*(?:강의동|동)?\s*[-\s]?\s*(\d{2,3})\s*호?', text)
    if m:
        return m.group(1) + m.group(2)
    m2 = re.search(r'(\d{3,4})\s*호?', text)
    if m2:
        return m2.group(1)
    return None


def has_conflict(field: str, value: str, start, end):
    conflicts = db.collection("Reservations") \
        .where("startTimestamp", "<", end) \
        .where("endTimestamp", ">", start) \
        .where(field, "==", value).stream()
    logging.info(f"[has_conflict] field={field}, value={value}")
    return any(True for _ in conflicts)

def handle_query_equipment(query, userID):
    doc = db.collection("rooms").document(query["room"]).get()
    if not doc.exists:
        return https_fn.Response("강의실이 존재하지 않습니다.", status=404)
    eq = doc.to_dict().get("equipment", [])
    item = query.get("item")
    if not item or item in ["뭐", "무엇", "있는지", "있는가"]:
        eq_list = ", ".join(eq) if eq else "없음"
        return https_fn.Response(f"{query['room']}호에 있는 기자재: {eq_list}", status=200)
    return https_fn.Response(f"{query['room']}호에 '{item}'이(가) {'있습니다' if item in eq else '없습니다' }.", status=200)


def handle_reserve(query, userID):
    try:
        logging.info(f"[handle_reserve] input query: {query}")
        query["userID"] = userID

        room_raw = query.get("room")
        query["room"] = extract_room_id(room_raw) if room_raw else None

        if not query["room"] or not query.get("startTime") or not query.get("duration"):
            pending = db.collection("PendingReservations").document(userID).get()
            if pending.exists:
                pending_data = pending.to_dict()
                query["room"] = query["room"] or pending_data.get("room")
                query["startTime"] = query.get("startTime") or pending_data.get("startTime")
                query["duration"] = query.get("duration") or pending_data.get("duration")
                query["eventName"] = query.get("eventName") or pending_data.get("eventName", "추천 예약")
                query["eventParticipants"] = query.get("eventParticipants") or pending_data.get("eventParticipants")
            else:
                if not query["room"]:
                    return https_fn.Response("강의실 정보를 확인할 수 없어요.", status=400)
                now = datetime.now(KST)
                query["startTime"] = (now + timedelta(minutes=10)).isoformat(timespec="seconds")
                query["duration"] = 2
                query["eventName"] = query.get("eventName", "추천 예약")
                query["eventParticipants"] = query.get("eventParticipants")
                logging.info("[handle_reserve] Pending 없이 기본값으로 예약 진행")

        query["eventName"] = "추천 예약"
        query["eventDescription"] = ""
        query["eventTarget"] = ""
        
        # 'eventParticipants' 키의 값을 가져옵니다.
        event_participants_value = query.get("eventParticipants")

        # 값이 문자열인 경우, 공백을 제거합니다.
        if isinstance(event_participants_value, str):
            query["eventParticipants"] = event_participants_value.strip()
        # 값이 None이거나 다른 타입인 경우, 안전하게 문자열로 변환하고 빈 문자열로 처리할 수 있습니다.
        # LLM이 인원수를 숫자로 반환할 수도 있으므로, 이를 고려합니다.
        else:
            # 값이 None이면 빈 문자열로, 아니면 문자열로 변환합니다.
            query["eventParticipants"] = str(event_participants_value or "").strip()
            
        query["status"] = "대기"

        required_fields = ["room", "startTime", "duration", "userID", "eventParticipants"]
        missing = [f for f in required_fields if not query.get(f) or str(query.get(f)).strip() == ""]
        if missing:
            db.collection("PendingReservations").document(userID).set(query)
            friendly_names = {
                "room": "강의실",
                "startTime": "시작 시간",
                "duration": "이용 시간",
                "userID": "사용자 정보",
                "eventParticipants": "이용 인원 수"
            }
            readable = ", ".join(friendly_names.get(f, f) for f in missing)
            return https_fn.Response(f"다음 정보가 필요해요: {readable}", status=400)

        query["room"] = extract_room_id(query["room"]) or query["room"]

        room_doc = db.collection("rooms").document(query["room"]).get()
        if not room_doc.exists:
            logging.warning(f"[handle_reserve] 존재하지 않는 강의실: {query['room']}")
            return https_fn.Response("해당 강의실은 존재하지 않습니다.", status=404)

        try:
            query["duration"] = int(query["duration"])
            start = datetime.fromisoformat(query["startTime"])
            if start.tzinfo is None:
                start = start.replace(tzinfo=timezone.utc).astimezone(KST)
        except Exception as e:
            logging.exception(f"[handle_reserve] 시간 파싱 실패: {query.get('startTime')}")
            return https_fn.Response("시작 시간이 올바른 형식이 아니에요.", status=400)

        if query["duration"] < 1 or query["duration"] > 6:
            return https_fn.Response("예약 시간은 최소 1시간, 최대 6시간까지만 가능합니다.", status=400)

        now = datetime.now(KST)
        if start < now:
            return https_fn.Response("예약 시작 시간은 현재 시간 이후여야 해요.", status=400)

        end = start + timedelta(hours=query["duration"])

        if has_conflict("userID", userID, start, end):
            return https_fn.Response("해당 시간에 이미 예약한 강의실이 있어요. 다른 시간대를 선택해 주세요.", status=409)
        if has_conflict("roomID", query["room"], start, end):
            return https_fn.Response(f"{query['room']}호는 해당 시간에 이미 예약되어 있어요.", status=409)

        try:
            # 안전하게 eventParticipants를 정수로 변환
            participants_str = str(query.get("eventParticipants", "0")).strip()
            # 숫자만 추출
            numeric_part = re.search(r'\d+', participants_str)
            event_participants_int = int(numeric_part.group()) if numeric_part else 1

            # 시간 문자열 생성 후 한국어 형식으로 변환
            start_str = start.strftime("%Y년 %-m월 %-d일 %p %-I시 %M분 %S초 UTC+9").replace("AM", "오전").replace("PM", "오후")
            end_str = end.strftime("%Y년 %-m월 %-d일 %p %-I시 %M분 %S초 UTC+9").replace("AM", "오전").replace("PM", "오후")

            doc_ref = db.collection("Reservations").add({
                "roomID": query["room"],
                "startTime": start_str,
                "endTime": end_str,
                "startTimestamp": start,
                "endTimestamp": end,
                "eventName": query.get("eventName", "AI 추천 예약"),
                "eventDescription": query.get("eventDescription", ""),
                "eventTarget": query.get("eventTarget", ""),
                "eventParticipants": event_participants_int, # 정수형으로 저장
                "status": query.get("status", "대기"),
                "userID": userID
            })
        except Exception as e:
            logging.exception("[handle_reserve] 예약 저장 실패")
            return https_fn.Response("예약 저장 중 오류가 발생했어요.", status=500)

        try:
            db.collection("PendingReservations").document(userID).delete()
        except Exception as e:
            logging.warning(f"[handle_reserve] Pending 삭제 실패: {e}")

        logging.info(f"[handle_reserve] 예약 성공: {doc_ref[1].id}")
        return https_fn.Response(f"{query['room']}호가 예약되었습니다 ✅", status=200)

    except Exception as e:
        logging.exception("[handle_reserve] 최상위 예외 발생")
        return https_fn.Response("예약 처리 중 알 수 없는 오류가 발생했어요. 로그를 확인해 주세요.", status=500)



def handle_cancel_reservation(query, userID):
    try:
        room_id = extract_room_id(query.get("room", ""))
        logging.info(f"[handle_cancel_reservation] room={room_id}, userID={userID}")

        # 가장 최근 예약 1건 조회 (startTime 기준)
        col = db.collection("Reservations").where("userID", "==", userID)
        if room_id:
            col = col.where("roomID", "==", room_id)
        
        # .limit(1)을 추가하여 가장 최근 예약 1건만 가져옴
        docs_query = col.order_by("startTimestamp", direction=firestore.Query.DESCENDING).limit(1)

        try:
            # 쿼리를 실행하여 문서 목록을 가져옴
            docs = list(docs_query.stream())
        except Exception as e:
            logging.exception("[handle_cancel_reservation] 예약 조회 실패")
            return https_fn.Response("예약 조회 중 문제가 발생했어요. 로그를 확인해주세요.", status=500)

        if not docs:
            return https_fn.Response("취소할 예약이 없습니다.", status=404)

        # 첫 번째 (가장 최근) 문서만 처리
        doc_to_delete = docs[0]
        cancelled_room = doc_to_delete.to_dict().get("roomID", "해당 강의실")
        db.collection("Reservations").document(doc_to_delete.id).delete()

        return https_fn.Response(f"{cancelled_room}호 예약이 취소되었습니다 ✅", status=200)

    except Exception as e:
        logging.exception("[handle_cancel_reservation] 예외 발생")
        return https_fn.Response("예약 취소 중 오류가 발생했어요. 로그를 확인해주세요.", status=500)

def handle_latest_notice(query, userID):
    docs = db.collection("Notices").order_by("createdAt", direction=firestore.Query.DESCENDING).limit(1).get()
    if not docs:
        return https_fn.Response("공지사항이 없습니다.", status=200)
    notice = docs[0].to_dict()
    return https_fn.Response(f"[{notice['title']}]\n{notice['content']}", status=200)

def handle_my_reviews(query, userID):
    docs = db.collection("Reviews").where("userID", "==", userID).stream()
    reviews = [d.to_dict() for d in docs]
    if not reviews:
        return https_fn.Response("작성한 리뷰가 없습니다.", status=200)
    sorted_reviews = sorted(reviews, key=lambda x: x.get("createdAt", datetime.min), reverse=True)
    preview = sorted_reviews[:2]
    reply_lines = [f"{r.get('roomID', '?')}호: {r.get('comment', '')} (★{r.get('rating', '?')})" for r in preview]
    reply_lines.append("\n자세한 리뷰는 마이페이지에서 확인해 주세요 😊")
    return https_fn.Response("\n\n".join(reply_lines), status=200)

def handle_review_summary(query, userID):
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

def handle_recommend_room(query, userID):
    keywords = query.get("keywords", [])
    now = datetime.utcnow() + timedelta(hours=9)

    # 🔍 인원 조건 추출
    person_count = next(
        (int(k.replace("명", "")) for k in keywords if k.endswith("명") and k[:-1].isdigit()), 
        None
    )

    # 🔍 시간 기준: "지금" 또는 명시적 afterTime
    require_available_now = "지금" in keywords
    after_time_str = query.get("afterTime")
    base_time = now

    if after_time_str:
        try:
            base_time = datetime.fromisoformat(after_time_str)
            require_available_now = True  # afterTime이 있으면 무조건 검사
        except:
            pass  # 시간 형식이 잘못된 경우 무시하고 now로 진행

    matched = []

    for doc in db.collection("rooms").stream():
        room_id = doc.id
        data = doc.to_dict()
        eq = data.get("equipment", [])
        cap = data.get("capacity", 0)

        # 인원 조건
        if person_count is not None and cap < person_count:
            continue

        # ✅ base_time에 사용 중인지 확인
        if require_available_now:
            conflict = db.collection("Reservations") \
                .where("roomID", "==", room_id) \
                .where("startTimestamp", "<=", base_time) \
                .where("endTimestamp", ">", base_time) \
                .get()
            if conflict:
                continue

        # 기자재 키워드 일치 점수 계산
        score = sum(1 for k in keywords if k in eq)

        if score > 0 or person_count is not None or require_available_now:
            matched.append((score, room_id, data))

    if not matched:
        return https_fn.Response("조건에 맞는 강의실이 없어요 😥", status=200)

    matched.sort(reverse=True)
    _, room_id, best = matched[0]

    location = best.get("location", "정보 없음")
    capacity = best.get("capacity", "정보 없음")
    equipment = ", ".join(best.get("equipment", [])) or "없음"

    # 🔍 후기 분석
    reviews = db.collection("Reviews").where("roomID", "==", room_id).stream()
    ratings, pos_comments, neg_comments, latest_comment, latest_time = [], [], [], None, None
    for r in reviews:
        review = r.to_dict()
        rating = review.get("rating")
        comment = review.get("comment", "")
        created = review.get("createdAt")
        if rating is not None:
            ratings.append(rating)
            if rating >= 4:
                pos_comments.append(comment)
            elif rating <= 2:
                neg_comments.append(comment)
        if created and comment and (not latest_time or str(created) > str(latest_time)):
            latest_comment, latest_time = comment, created

    avg = round(sum(ratings) / len(ratings), 1) if ratings else None
    pos_rate = round(len(pos_comments) / len(ratings) * 100) if ratings else 0
    neg_rate = 100 - pos_rate if ratings else 0

    response = f"""조건에 맞는 강의실을 찾아봤어요! 😊
📍 위치: {location}
🏫 강의실: {room_id}호 (최대 {capacity}명)
🛠️ 기자재: {equipment}"""
    if latest_comment:
        response += f'\n📝 최근 후기: "{latest_comment}"'
    if avg is not None:
        response += f"\n⭐ 평균 평점: {avg}점\n📊 긍정 {pos_rate}%, 부정 {neg_rate}%"

    pending_data = {
        "room": room_id,
        "startTime": (base_time + timedelta(minutes=10)).astimezone(KST).isoformat(),
        "duration": 2,
        "eventName": "추천 예약"
    }
    if person_count is not None:
        pending_data["eventParticipants"] = f"{person_count}명"

    db.collection("PendingReservations").document(userID).set(pending_data)

    return https_fn.Response(response, status=200)


def handle_list_rooms(query, userID):
    docs = db.collection("rooms").stream()
    rooms = [doc.id for doc in docs]
    return https_fn.Response("전체 강의실: " + ", ".join(rooms), status=200)

def handle_list_rooms_by_building(query, userID):
    target = query.get("building")
    if not target:
        return https_fn.Response("건물명을 입력해 주세요. 예: '5강의동'", status=400)
    docs = db.collection("rooms").where("buildingDetail", "==", target).stream()
    room_ids = [doc.id for doc in docs]
    if not room_ids:
        return https_fn.Response(f"'{target}'에 해당하는 강의실이 없어요.", status=200)
    return https_fn.Response(f"{target}의 강의실 목록: {', '.join(room_ids)}", status=200)

def handle_list_rooms_by_equipment(query, userID):
    item = query.get("item")
    if not item:
        return https_fn.Response("기자재를 입력해 주세요. 예: '마이크'", status=400)
    docs = db.collection("rooms").where("equipment", "array_contains", item).stream()
    room_ids = [doc.id for doc in docs]
    if not room_ids:
        return https_fn.Response(f"'{item}'이(가) 있는 강의실이 없어요.", status=200)
    return https_fn.Response(f"'{item}'이(가) 있는 강의실: {', '.join(room_ids)}", status=200)

def handle_room_availability(query, userID):
    room = query.get("room")
    if not room:
        return https_fn.Response("강의실 번호를 입력해 주세요.", status=400)
    now = datetime.utcnow(KST)
    one_day_later = now + timedelta(days=1)
    docs = db.collection("Reservations") \
        .where("roomID", "==", room) \
        .where("startTime", ">", now) \
        .where("startTime", "<", one_day_later.isoformat()).stream()
    times = [d.to_dict().get("startTime", "?") for d in docs]
    if not times:
        return https_fn.Response(f"{room}호는 앞으로 24시간 예약이 없습니다.", status=200)
    return https_fn.Response(f"{room}호 예약 시간 목록: {', '.join(times)}", status=200)

def handle_my_reservations(query, userID):
    docs = db.collection("Reservations").where("userID", "==", userID).stream()
    res_list = []
    for d in docs:
        data = d.to_dict()
        res_list.append(f"{data.get('roomID', '?')}호 ({data.get('startTime', '?')} ~ {data.get('endTime', '?')})")
    if not res_list:
        return https_fn.Response("예약된 강의실이 없습니다.", status=200)
    return https_fn.Response("내 예약 내역:\n" + "\n".join(res_list), status=200)

handlers = {
    "query_equipment": handle_query_equipment,
    "reserve": handle_reserve,
    "cancel_reservation": handle_cancel_reservation,
    "latest_notice": handle_latest_notice,
    "my_reviews": handle_my_reviews,
    "recommend_room": handle_recommend_room,
    "review_summary": handle_review_summary,
    "list_rooms": handle_list_rooms,
    "list_rooms_by_building": handle_list_rooms_by_building,
    "list_rooms_by_equipment": handle_list_rooms_by_equipment,
    "room_availability": handle_room_availability,
    "my_reservations": handle_my_reservations
}

@https_fn.on_request()
def ai_assistant(req: https_fn.Request) -> https_fn.Response:
    try:
        data = req.get_json()
        user_input = data.get("message", "")

        id_token = req.headers.get("Authorization", "").replace("Bearer ", "")
        uid = "unknown"
        if id_token:
            try:
                decoded_token = auth.verify_id_token(id_token)
                uid = decoded_token.get("uid", "unknown")
            except Exception as e:
                logging.warning(f"Failed to verify token: {e}")
                return https_fn.Response("유효하지 않은 토큰입니다.", status=401)
        
        if uid == "unknown":
            logging.warning("UserID is unknown. Authentication is required.")
            return https_fn.Response("사용자 인증이 필요합니다. 다시 로그인해주세요.", status=401)

        # uid로 사용자 문서 조회하여 학번(studentId) 가져오기
        try:
            user_doc_ref = db.collection("User").document(uid)
            user_doc = user_doc_ref.get()
            if user_doc.exists:
                user_data = user_doc.to_dict()
                userID = user_data.get("studentId") # 학번을 userID로 사용
                if not userID:
                    logging.error(f"studentId not found for uid: {uid}")
                    return https_fn.Response("사용자 정보에서 학번을 찾을 수 없습니다.", status=404)
            else:
                logging.error(f"User document not found for uid: {uid}")
                return https_fn.Response("사용자 정보를 찾을 수 없습니다.", status=404)
        except Exception as e:
            logging.exception("Failed to fetch user data from Firestore.")
            return https_fn.Response("사용자 정보 조회 중 오류가 발생했습니다.", status=500)

        # ✅ 단순 반응 처리 (GPT 호출 전)
        positive_keywords = ["응", "ㅇㅇ", "좋아", "그래", "예약해줘", "해줘", "할래"]
        if user_input.strip() in positive_keywords:
            pending = db.collection("PendingReservations").document(userID).get()
            if pending.exists:
                logging.info("[단순 반응에 의한 예약 진행]")
                pending_data = pending.to_dict()
                query = {
                    "action": "reserve",
                    "userID": userID,
                    "room": pending_data.get("room"),
                    "startTime": pending_data.get("startTime"),
                    "duration": pending_data.get("duration"),
                    "eventName": pending_data.get("eventName", "추천 예약")
                }
                return handle_reserve(query, userID)
            else:
                return https_fn.Response("추천된 강의실이 없어요. 먼저 추천을 받아주세요 😊", status=400)

        # 시스템 프롬프트
        now_kst = datetime.now(KST)
        system_prompt = f"""너는 Firestore 기반 강의실 예약 도우미야. 사용자의 한국어 문장을 먼저 자연스럽게 오타 없이 교정하고, 그 다음 아래 JSON 명령 중 하나로 변환해.

오늘 날짜는 {now_kst.strftime('%Y-%m-%d')}이야. 이 정보를 바탕으로 '내일', '모레' 같은 상대적인 날짜를 정확한 ISO 8601 형식의 시간으로 변환해줘.

반드시 아래 형식을 따르고, **JSON만 반환**해야 해.
설명, 문장, 주석 등은 출력하지 마. 오직 JSON 한 개만 반환해.

⚠️ 중요: 다음과 같은 응답은 절대 하지 마세요:
- "알겠습니다. 아래 JSON을 참고하세요." 같은 자연어 포함
- JSON 앞뒤에 설명 추가
- JSON 블록이 아닌 텍스트만 반환

---

### 명령 유형과 JSON 구조

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
"startTime": "{now_kst.isoformat()}",
"duration": 2,
"eventParticipants": "6명",
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
"keywords": ["6명", "TV", "마이크"],
"startTime": "{now_kst.isoformat()}"
}}

7. 강의실 평가 요청
{{
"action": "review_summary",
"room": "5104"
}}

8. 전체 강의실 조회
{{
"action": "list_rooms"
}}

9. 동별 강의실 조회
{{
"action": "list_rooms_by_building",
"building": "5강의동"
}}

10. 기자재별 강의실 조회
{{
"action": "list_rooms_by_equipment",
"item": "마이크"
}}

11. 특정 강의실 예약 확인
{{
"action": "room_availability",
"room": "5104"
}}

12. 내 예약 확인
{{
"action": "my_reservations"
}}

---

### 추가 응답 예약 예외 규칙

사용자의 입력이 긍정 반응("응", "좋아요", "ㅇㅇ", "그래", "예약해줘" 등) **단순 반응**일 경우:

- **최근에 추천된 강의실이 존재할 때만** 아래 JSON을 반환해야 해:

{{
"action": "reserve",
"userID": "{userID}"
}}

- 추천된 강의실이 없다면 아무것도 반환하지 마.

---

### recommend_room의 keywords 규칙

- 무조건 문자열 리스트(List[str])로 작성
- 키워드는 다음 중 포함 가능:
  - 인원수: "6명", "8명" 등
  - 기자재: "TV", "마이크", "전자칠판", "빔프로젝터"
  - 시간: "지금"

시간 조건이 있는 경우 다음 형식을 따름:
- `"afterTime": "YYYY-MM-DDTHH:MM:SS"` 형태로 특정 시점 이후 가능한 강의실 추천 가능
"""


        gpt = client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_input}
            ]
        )

        gpt_response_text = gpt.choices[0].message.content.strip()

        logging.info(f"[user_input]: {user_input}")
        logging.info(f"[gpt_response_text]: {gpt_response_text}")

        if not gpt_response_text.startswith("{") or not gpt_response_text.endswith("}"):
            logging.warning(f"[GPT 응답 JSON 아님]: {gpt_response_text}")

            if any(k in user_input for k in ["추천", "추천해줘", "예약까지", "추천하고 예약"]):
                logging.info("[추천 문장 fallback 처리 진행]")
                _ = handle_recommend_room({"keywords": []}, userID)
                pending = db.collection("PendingReservations").document(userID).get()
                if pending.exists:
                    pending_data = pending.to_dict()
                    reserve_query = {
                        "action": "reserve",
                        "userID": userID,
                        "room": pending_data.get("room"),
                        "startTime": pending_data.get("startTime"),
                        "duration": pending_data.get("duration"),
                        "eventName": pending_data.get("eventName", "추천 예약")
                    }
                    return handle_reserve(reserve_query, userID)

            pending = db.collection("PendingReservations").document(userID).get()
            if pending.exists:
                logging.info("[예약 단순 확인] GPT 없이 Pending 기반 예약 진행")
                pending_data = pending.to_dict()
                query = {
                    "action": "reserve",
                    "userID": userID,
                    "room": pending_data.get("room"),
                    "startTime": pending_data.get("startTime"),
                    "duration": pending_data.get("duration"),
                    "eventName": pending_data.get("eventName", "추천 예약")
                }
                return handle_reserve(query, userID)

            return https_fn.Response("죄송해요, 이해하지 못했어요. 예: '5104호 예약해줘'처럼 다시 말해 주세요 😊", status=400)

        try:
            query = json.loads(gpt_response_text)
            logging.info(f"[query]: {query}")
        except json.JSONDecodeError:
            logging.warning(f"[GPT JSON 파싱 실패]: {gpt_response_text}")
            return https_fn.Response("죄송해요, 응답 처리에 실패했어요. 다시 시도해 주세요.", status=400)

        action = query.get("action")
        if not action or action not in handlers:
            return https_fn.Response("죄송해요, 요청을 이해하지 못했어요 😥", status=400)

        if "room" in query and query["room"]:
            query["room"] = extract_room_id(query["room"]) or query["room"]

        if action == "recommend_room":
            response = handle_recommend_room(query, userID)
            pending = db.collection("PendingReservations").document(userID).get()
            if pending.exists and any(kw in user_input for kw in ["예약까지", "예약해줘", "바로 예약", "바로해줘"]):
                pending_data = pending.to_dict()
                reserve_query = {
                    "action": "reserve",
                    "userID": userID,
                    "room": pending_data.get("room"),
                    "startTime": pending_data.get("startTime"),
                    "duration": pending_data.get("duration"),
                    "eventName": pending_data.get("eventName", "추천 예약")
                }
                return handle_reserve(reserve_query, userID)
            return response

        return handlers[action](query, userID)

    except Exception as e:
        logging.exception("예외 발생:")
        return https_fn.Response("알 수 없는 오류가 발생했어요. 잠시 후 다시 시도해 주세요.", status=500)
