# MiRam AOS

MiRam은 Jetpack Compose 기반의 Android 알람 앱입니다. 정확한 시간에 울리는 알람을 중심으로, 반복 요일, 특정 날짜, 다시 울림, 진동, 알림음, 울림 시간, 잠금 화면 표시까지 지원합니다.

## 현재 상태

- 버전: `1.0.1`
- 버전 코드: `4`
- `applicationId`: `com.seungmin.miram`
- `minSdk`: `26`
- `targetSdk`: `35`

## 핵심 기능

- 알람 추가, 수정, 삭제, ON/OFF 토글
- 반복 요일 기반 알람
- 특정 날짜 기반 1회성 알람
- 스누즈 간격 / 반복 횟수 설정
- 진동 on/off 및 진동 패턴 설정
- 시스템 알림음 선택
- 울림 시간 설정
  - `0초 = 계속 울림`
- 재부팅 후 활성 알람 자동 재등록
- 포그라운드 서비스 기반 알람 울림 처리
- full-screen intent 기반 잠금 화면 알람 표시

## 기술 스택

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- Hilt
- Room
- Kotlin Coroutines / Flow
- `AlarmManager.setAlarmClock(...)`

## 프로젝트 구조

이 프로젝트는 단일 `:app` 모듈 기반이지만, 코드 레벨에서는 기능별 폴더를 루트에 평면 배치하고 `sourceSets`로 묶는 구조입니다.

```text
app/        AndroidManifest, Activity, 리소스
features/   화면별 Compose UI 및 ViewModel
routes/     내비게이션 루트
shared/     알람 런타임, 데이터, 모델, DI, 스타일
```

주요 폴더 역할:

- `features/main/home`
  - 홈 화면, 다음 알람 요약, 목록 정렬, 다중 선택
- `features/main/alarmdetail`
  - 알람 생성/수정 화면
- `features/main/alarmringing`
  - 알람 울림 화면
- `shared/alarm`
  - 스케줄러, 브로드캐스트 리시버, 포그라운드 서비스, 권한 요구사항
- `shared/data`
  - Room DAO / Database / Repository
- `shared/model`
  - 알람 모델과 시간 계산 로직
- `shared/style`
  - 테마, 색상, 폰트 스케일 정책

## 아키텍처 흐름

### 저장 흐름

`AlarmDetailScreen`
-> `AlarmDetailViewModel`
-> `AlarmRepository`
-> `Room`
-> `AlarmScheduler`

### 울림 흐름

`AlarmManager`
-> `AlarmReceiver`
-> `AlarmForegroundService`
-> `AlarmNotificationHelper`
-> `AlarmStateHolder`
-> `MainRoute`
-> `AlarmRingingScreen`

### 재부팅 복구 흐름

`BOOT_COMPLETED`
-> `BootReceiver`
-> `AlarmRepository`
-> `AlarmScheduler`

## 알람 동작 방식

MiRam은 일반 백그라운드 작업이 아니라 exact alarm 중심으로 동작합니다.

- 예약 등록은 `AlarmManager.setAlarmClock(...)` 기반
- 반복 알람은 요일별 개별 `PendingIntent`로 예약
- 저장/수정 시 기존 예약을 취소한 뒤 최신 값으로 재등록
- 스누즈도 별도 exact alarm로 다시 등록
- 알람 발생 시 `AlarmReceiver`가 `ForegroundService`를 시작
- 서비스가 소리, 진동, 알림, 스누즈를 관리
- `AlarmStateHolder`를 통해 UI를 울림 화면으로 전환

## 권한 및 시스템 요구사항

Manifest 기준 주요 권한:

- `USE_EXACT_ALARM`
- `SCHEDULE_EXACT_ALARM` with `maxSdkVersion=32`
- `RECEIVE_BOOT_COMPLETED`
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
- `USE_FULL_SCREEN_INTENT`
- `POST_NOTIFICATIONS`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `VIBRATE`
- `WAKE_LOCK`

저장 화면에서는 아래 항목을 점검합니다.

- exact alarm 권한
- full-screen intent 권한
- 배터리 최적화 제외 여부

## 디자인 / UX 특징

- 강조 색상: 강한 핑크 계열
- 라이트 / 다크 테마 모두 지원
- 홈 화면에서 다음 알람까지 남은 시간 요약 제공
- 알람 울림 화면에서 남은 시간과 해제 버튼 제공
- 폰트 스케일은 고정 정책 사용

## 개발 환경

- Android Studio
- JDK 17
- Gradle 8.x
- Kotlin JVM target 17

## 실행 방법

디버그 빌드:

```bash
./gradlew :app:assembleDebug
```

디버그 Kotlin 컴파일 확인:

```bash
./gradlew :app:compileDebugKotlin
```

릴리즈 AAB 빌드:

```bash
./gradlew :app:bundleRelease
```

릴리즈 서명을 위해서는 `key.properties`가 필요합니다.

## 릴리즈 관련

- 릴리즈 버전은 `gradle.properties`에서 관리
- 릴리즈 서명은 `key.properties`에서 읽음
- release build는 code shrinking / resource shrinking 활성화
- Google Play 배포 절차는 `PLAY_RELEASE.md`에 정리되어 있음

MiRam은 exact alarm을 핵심 기능으로 사용하는 앱이므로, Google Play 배포 시 관련 권한 선언을 정확히 작성해야 합니다.

## 문서

- 프로젝트 전체 분석: [`PROJECT_ANALYSIS.md`](./PROJECT_ANALYSIS.md)
- Google Play 릴리즈 가이드: [`PLAY_RELEASE.md`](./PLAY_RELEASE.md)

## 현재 보이는 개선 포인트

- Room이 `fallbackToDestructiveMigration()`을 사용하므로 스키마 변경 시 데이터 유실 가능성 있음
- 테스트 코드가 거의 없어서 시간 계산, 스누즈, 재부팅 복구 회귀 검증이 약함
- “직접설정한 순서” 정렬은 실제 사용자 재정렬이 아니라 `createdAt` 기반
- 폰트 스케일 고정은 접근성 측면에서 trade-off가 있음

## 한 줄 요약

MiRam AOS는 exact alarm을 중심으로 설계된 Android 알람 앱이며, 저장부터 울림까지의 흐름이 비교적 명확하게 정리된 코드베이스입니다.
