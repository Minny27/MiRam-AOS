# MiRam AOS

MiRam은 Jetpack Compose 기반의 안드로이드 알람 앱입니다. 단발성 알람과 반복 알람을 지원하고, 앱이 종료되거나 기기가 재부팅된 뒤에도 예약 상태를 다시 복구하도록 구성되어 있습니다.

## 주요 기능

- 알람 추가, 수정, 삭제, ON/OFF 토글
- 반복 요일 기반 알람과 특정 날짜 알람 지원
- 스누즈, 진동, 벨소리, 울림 시간 설정 지원
- 포그라운드 서비스 기반 알람 울림 처리
- 부팅 완료 후 활성 알람 자동 재등록

## 최근 반영 내용

### 1. 배터리 최적화 무시 권한 안내 추가

정확한 시간에 알람이 동작하도록 저장 시 런타임 요구사항을 확인합니다.

- Android 12 이상에서는 정확한 알람 권한 필요 여부를 확인합니다.
- Android 6 이상에서는 배터리 최적화 제외 상태를 확인합니다.
- 배터리 최적화가 활성화되어 있으면 설정 화면으로 유도해 앱을 최적화 대상에서 제외할 수 있게 했습니다.
- 일부 제조사 환경에서 직접 요청 인텐트를 처리하지 못하는 경우를 대비해 일반 배터리 최적화 설정 화면으로 폴백합니다.

관련 코드:

- `app/src/main/AndroidManifest.xml`
- `shared/alarm/AlarmRuntimeRequirements.kt`
- `features/main/alarmdetail/AlarmDetailScreen.kt`

### 2. 백그라운드 알람 예약 안정성 개선

백그라운드 상태에서도 예약 누락 가능성을 줄이기 위해 `AlarmManager.setAlarmClock(...)` 기반으로 예약하도록 정리되어 있습니다.

- 단발 알람은 다음 트리거 시각으로 1회 예약합니다.
- 반복 알람은 선택한 요일마다 개별 `PendingIntent`를 생성해 예약합니다.
- 알람을 다시 저장할 때 기존 예약을 먼저 취소한 뒤 최신 값으로 재등록합니다.
- 재부팅 이후에는 `BootReceiver`가 저장된 활성 알람을 다시 읽어 전체 재예약합니다.
- 스누즈 알람도 동일하게 `AlarmManager`를 통해 다시 예약합니다.

관련 코드:

- `shared/alarm/AlarmScheduler.kt`
- `shared/alarm/BootReceiver.kt`
- `shared/alarm/AlarmReceiver.kt`
- `shared/alarm/AlarmForegroundService.kt`

## 개발 환경

- Android Studio
- JDK 17
- `minSdk 26`
- `targetSdk 35`
- Kotlin + Jetpack Compose
- Hilt
- Room

## 실행 방법

```bash
./gradlew :app:assembleDebug
```

릴리즈 빌드와 Play 배포 관련 내용은 `PLAY_RELEASE.md`를 참고하면 됩니다.

## 프로젝트 구조

```text
app/        AndroidManifest, Activity, 리소스
features/   화면 단위 UI 및 ViewModel
routes/     네비게이션 라우트
shared/     알람, 데이터, 모델, DI, 스타일 공통 모듈
```
