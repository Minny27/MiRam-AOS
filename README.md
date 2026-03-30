# MiRam AOS

MiRam은 Jetpack Compose 기반의 Android 알람 앱입니다. exact alarm을 중심으로 동작하며, 알람 저장부터 예약, 울림, 재부팅 후 복구까지 한 흐름으로 구성되어 있습니다.

## 현재 기준

- 앱 이름: `MiRam`
- 패키지명: `com.seungmin.miram`
- 버전: `1.0.2`
- 버전 코드: `4`
- `minSdk`: `26`
- `targetSdk`: `35`
- `compileSdk`: `35`
- Kotlin / JVM target: `17`

## 주요 기능

- 알람 추가, 수정, 삭제, ON/OFF 토글
- 반복 요일 알람
- 특정 날짜 1회성 알람
- 알림음 선택
- 진동 ON/OFF
- 스누즈 간격 / 반복 횟수 설정
- 울림 시간 설정
  - `0초`는 사용자가 직접 해제할 때까지 계속 울림
- 홈 화면의 다음 알람 요약 표시
- 알람 울림 화면 전환
- 재부팅 후 활성 알람 자동 재등록

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

이 프로젝트는 Gradle 기준으로는 단일 `:app` 모듈입니다. 다만 코드 파일은 루트 하위 디렉토리에 분리되어 있고, `app/build.gradle.kts`의 `sourceSets`로 하나의 앱 모듈에 연결됩니다.

```text
app/        AndroidManifest, Activity, 리소스, Gradle 모듈
features/   화면별 Compose UI / ViewModel
routes/     Navigation 진입점
shared/     알람 런타임, 데이터, 모델, DI, 스타일
```

주요 디렉토리:

- `features/main/home`
  - 알람 목록, 다음 알람 요약, 선택 편집, 정렬
- `features/main/alarmdetail`
  - 알람 생성 / 수정, 권한 점검, 저장
- `features/main/alarmringing`
  - 울림 화면
- `shared/alarm`
  - 스케줄링, 브로드캐스트 리시버, 포그라운드 서비스, 런타임 요구사항
- `shared/data`
  - Room DB, DAO, Repository
- `shared/model`
  - 알람 모델, 반복 요일, 울림 시간 계산
- `shared/style`
  - 테마, 색상, 폰트 설정

## 동작 흐름

저장 흐름:

`AlarmDetailScreen` -> `AlarmDetailViewModel` -> `AlarmRepository` -> `Room` -> `AlarmScheduler`

울림 흐름:

`AlarmManager` -> `AlarmReceiver` -> `AlarmForegroundService` -> `AlarmStateHolder` -> `MainRoute` -> `AlarmRingingScreen`

재부팅 복구 흐름:

`BOOT_COMPLETED` -> `BootReceiver` -> `AlarmRepository` -> `AlarmScheduler`

## 권한 및 시스템 요구사항

Manifest 기준 주요 권한:

- `USE_EXACT_ALARM`
- `SCHEDULE_EXACT_ALARM` (`maxSdkVersion=32`)
- `RECEIVE_BOOT_COMPLETED`
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
- `USE_FULL_SCREEN_INTENT`
- `POST_NOTIFICATIONS`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `VIBRATE`
- `WAKE_LOCK`

알람 저장 시에는 아래 조건을 확인합니다.

- exact alarm 예약 가능 여부
- full-screen intent 사용 가능 여부
- 배터리 최적화 제외 여부

Android 버전에 따라 일부 요구사항은 체크되지 않습니다.

## 개발 환경

- Android Studio
- JDK 17
- Gradle 8.x

`gradle.properties`에 `org.gradle.java.home`이 Android Studio 내장 JBR 경로로 지정되어 있습니다. 환경이 다르면 이 값을 로컬에 맞게 조정해야 합니다.

## 빌드 및 실행

디버그 APK 빌드:

```bash
./gradlew :app:assembleDebug
```

디버그 Kotlin 컴파일 확인:

```bash
./gradlew :app:compileDebugKotlin
```

디바이스 설치:

```bash
./gradlew :app:installDebug
```

릴리즈 AAB 빌드:

```bash
./gradlew :app:bundleRelease
```

## 릴리즈 서명

릴리즈 서명 값은 아래 우선순위로 읽습니다.

1. Gradle property
2. 환경 변수
3. 루트의 `key.properties`

필요한 키:

- `storeFile`
- `storePassword`
- `keyAlias`
- `keyPassword`

모든 값이 있어야 release signing config가 활성화됩니다.

## 데이터 및 주의사항

- Room DB 버전은 현재 `3`
- 스키마 export는 비활성화되어 있음
- 마이그레이션 대신 `fallbackToDestructiveMigration()`을 사용하므로, 스키마 변경 시 기존 데이터가 삭제될 수 있음
- 중복 알람은 저장 시 동일 조건 기준으로 정리됨

## 문서

- 프로젝트 분석: [`PROJECT_ANALYSIS.md`](./PROJECT_ANALYSIS.md)
- Google Play 릴리즈 가이드: [`PLAY_RELEASE.md`](./PLAY_RELEASE.md)

## 알려진 한계

- 자동 테스트 코드가 거의 없어 시간 계산, 스누즈, 재부팅 복구 회귀 검증이 약함
- 정확한 알람과 full-screen intent는 제조사 / OS 정책 영향이 큼
- 배터리 최적화 설정 상태에 따라 실제 울림 안정성이 달라질 수 있음
