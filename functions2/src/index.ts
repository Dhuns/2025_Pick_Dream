import * as admin from "firebase-admin";
import { onSchedule } from "firebase-functions/v2/scheduler"; // v2 API 사용
import * as logger from "firebase-functions/logger"; // v2 권장 로거
import { QueryDocumentSnapshot } from "firebase-admin/firestore";

admin.initializeApp();

export const updateReservationStatusesV2 = onSchedule(
  {
    schedule: "every 5 minutes",
    timeZone: "Asia/Seoul",
    // v2에서는 onSchedule 인자로 스케줄 객체를 전달합니다.
  },
  async (event) => { // event 파라미터 (이전 context와 유사)
    logger.info("예약 상태 업데이트 함수 시작"); // eventId 제거
    const db = admin.firestore();
    const now = new Date(); // 현재 시간
    const batch = db.batch();
    let hasChanges = false; // 변경 사항 추적 플래그

    // 문자열 또는 Timestamp 타입 모두 파싱 가능한 함수
    const parseToDate = (input: any): Date => {
      if (!input) return new Date(NaN); // 유효하지 않은 입력 처리
      if (input instanceof admin.firestore.Timestamp) return input.toDate(); // Firestore Timestamp
      if (typeof input === "string") {
         // "yyyy년 M월 d일 a h시 m분 s초" 또는 "yyyy년 M월 d일 a h:m:s" 형식 파싱 시도
         // 한국어 로케일 고려 및 '오전/오후' 처리 필요
         // 예시: "2025년 6월 4일 오후 11시 13분 0초UTC+9" (UTC+9 제거 후 파싱 가정)
         const cleanedInput = input.replace(/\s*UTC\+9\s*$/, ""); // UTC+9 제거
         let amPmAdjustedInput = cleanedInput.replace("오전", "AM").replace("오후", "PM");
         
         // Date.parse가 잘 동작하도록 형식 변경 시도 (완벽하지 않을 수 있음)
         // "yyyy년 M월 d일 PM h시 m분 s초" -> "yyyy-M-d h:m:s PM"
         amPmAdjustedInput = amPmAdjustedInput
            .replace(/년\s*/, "-")
            .replace(/월\s*/, "-")
            .replace(/일\s*/, " ")
            .replace(/시\s*/, ":")
            .replace(/분\s*/, ":")
            .replace(/초\s*/, "");

         // PM/AM 위치 조정 시도: "2025-6-4 11:13:00 PM" -> "2025-6-4 PM 11:13:00" -> "06/04/2025 11:13:00 PM"
         // JS Date.parse는 MM/DD/YYYY hh:mm:ss AM/PM 형식에 더 관대함
         const parts = amPmAdjustedInput.split(" "); // ["2025-6-4", "PM", "11:13:00"]
         let parsedDate = new Date(NaN);
         if (parts.length === 3 && (parts[1] === "AM" || parts[1] === "PM")) {
            const dateParts = parts[0].split("-");
            const timeParts = parts[2].split(":");
            if (dateParts.length === 3 && timeParts.length >= 2) {
                 // MM/DD/YYYY 형식으로 재구성
                 const reformattedDateString = `${dateParts[1]}/${dateParts[2]}/${dateParts[0]} ${parts[2]} ${parts[1]}`;
                 parsedDate = new Date(reformattedDateString);
            }
         }

         if (!isNaN(parsedDate.getTime())) return parsedDate;

         // 표준 ISO 형식 등 다른 형식도 시도 (최후의 수단)
         const standardParsed = new Date(cleanedInput); // 원본 cleanedInput으로도 시도
         if(!isNaN(standardParsed.getTime())) return standardParsed;

         return new Date(NaN); // 모든 파싱 실패
      }
      return new Date(NaN); // 알 수 없는 타입
    };

    const snapshot = await db.collection("Reservations").get();

    logger.info(`조회된 예약 건수: ${snapshot.size}`);

    snapshot.forEach((doc: QueryDocumentSnapshot) => {
      const data = doc.data();
      const reservationId = doc.id; // 디버깅용

      const startTimeString = data.startTime;
      const endTimeString = data.endTime;

      const startDate = parseToDate(startTimeString);
      const endDate = parseToDate(endTimeString);

      if (isNaN(startDate.getTime()) || isNaN(endDate.getTime())) {
        logger.warn(`잘못된 날짜 형식 → 문서 ID: ${reservationId}`, { startTime: startTimeString, endTime: endTimeString });
        return;
      }

      let newStatus = "";
      if (now < startDate) newStatus = "대기";
      else if (now >= startDate && now <= endDate) newStatus = "확정"; // 시작 시간 포함
      else newStatus = "종료";

      const currentStatus = (data.status || "").trim();

      if (newStatus !== currentStatus) {
        batch.update(doc.ref, { status: newStatus });
        logger.info(`상태 변경: ${reservationId} (${currentStatus} → ${newStatus})`);
        hasChanges = true; // 변경 발생
      }
    });

    try {
      await batch.commit();
      if (hasChanges) { // hasChanges 플래그 사용
         logger.info(`✔ 예약 상태 자동 업데이트 완료 (커밋된 변경 존재)`);
      } else if (snapshot.size > 0) {
         logger.info(`예약 상태 변경 없음 (모든 예약 최신 상태).`);
      } else {
         logger.info("업데이트할 예약이 없습니다.");
      }
    } catch (error) {
      logger.error("예약 상태 업데이트 중 배치 커밋 실패:", error);
    }
  }
);
