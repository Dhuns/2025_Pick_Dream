# 강의실 예약 시스템 (Pick Dream)

## 프로젝트 개요
대학교 강의실 예약 및 관리 시스템입니다. 학생과 교직원이 강의실을 쉽게 예약하고 관리할 수 있는 플랫폼을 제공합니다.

## 주의사항
- 환경 변수 설정 필요합니다 (`env` , open ai cahtgpt api, google map api) , firebase는 유지



### 주요 디렉토리
- `app/`: 안드로이드 애플리케이션의 메인 소스 코드
  - `src/main/java/com/example/pick_dream/`: 메인 소스 코드
    - `model/`: 데이터 모델 클래스
    - `ui/`: 사용자 인터페이스 관련 코드
    - `domain/`: 비즈니스 로직
  - `src/main/res/`: 리소스 파일 (레이아웃, 이미지, 문자열 등)
  - `src/main/AndroidManifest.xml`: 앱의 기본 설정 파일

### 주요 모델
1. `LectureRoom`: 강의실 정보
   - 강의실 ID, 이름
   - 건물 정보 (이름, 상세 위치)
   - 수용 인원
   - 시설 정보 (프로젝터, 칠판 등)
   - 대여 가능 여부
   - 이미지 URL

2. `Reservation`: 예약 정보
   - 예약 ID
   - 사용자 ID
   - 강의실 ID
   - 이벤트 정보 (이름, 설명, 대상)
   - 참가자 수
   - 시작/종료 시간
   - 예약 상태

3. `User`: 사용자 정보
   - 사용자 ID
   - 기본 정보

4. `Review`: 강의실 리뷰
   - 리뷰 정보
   - 평점

### 주요 기능
1. 강의실 관리
   - 강의실 목록 조회
   - 강의실 상세 정보 확인
   - 시설 정보 확인
   - 즐겨찾기 기능

2. 예약 시스템
   - 강의실 예약
   - 예약 현황 확인
   - 예약 취소

3. 사용자 관리
   - 로그인
   - 마이페이지
   - 예약 내역 관리

4. 리뷰 시스템
   - 강의실 리뷰 작성
   - 리뷰 조회
   - 평점 시스템

## 기술 스택
- Android (Kotlin)
- Firebase
  - Authentication: 사용자 인증
  - Firestore: 데이터베이스
  - Storage: 이미지 저장
- MVVM 아키텍처
- Jetpack Compose
- Material Design 3

## 개발 환경 설정
1. Android Studio 설치
2. 프로젝트 클론
3. Firebase 프로젝트 설정
4. `google-services.json` 파일을 `app/` 디렉토리에 추가
5. Gradle 동기화
6. 앱 실행

## 주의사항
- Firebase 설정이 필요합니다
- 환경 변수 설정이 필요합니다 (`env` 파일 참조)
- 최소 SDK 버전: API 24 (Android 7.0)
- 강의실 예약 시 중복 예약 체크가 필요합니다
- 예약 시간은 학사 일정에 따라 제한될 수 있습니다
