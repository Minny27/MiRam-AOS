# MiRam AOS 프로젝트 분석

## 1. 프로젝트 개요

- 프로젝트명: `MiRam`
- 플랫폼: Android
- 성격: 정확한 시간 기반 알람 앱
- 현재 버전: `1.0.1`
- 현재 버전 코드: `4`
- 현재 앱 ID: `com.seungmin.miram`
- 최소 SDK: `26`
- 타깃 SDK: `35`

이 프로젝트는 Android의 exact alarm 기능을 핵심으로 사용하는 알람 앱이다. 단순히 시간을 저장하는 수준이 아니라, 반복 요일, 특정 날짜, 다시 울림, 진동 패턴, 알림음, 울림 지속 시간, 잠금 화면 표시까지 포함하는 구조로 구현되어 있다.

## 2. 기술 스택

- 언어: Kotlin
- UI: Jetpack Compose + Material 3
- DI: Hilt
- 로컬 저장소: Room
- 비동기 처리: Kotlin Coroutines / Flow
- 내비게이션: Navigation Compose
- 알람 실행: `AlarmManager.setAlarmClock(...)`
- 알람 울림 처리: `BroadcastReceiver` + `ForegroundService`

## 3. 저장소 구조

이 프로젝트는 멀티모듈이 아니라 단일 `:app` 모듈 기반이다. 다만 코드 구조는 `features`, `routes`, `shared`를 루트 레벨 폴더로 분리하고, `app/build.gradle.kts`의 `sourceSets`에서 이들을 함께 읽는 방식이다.

### 주요 디렉토리

- `app/src/main`
  - AndroidManifest, 앱 아이콘, 엔트리 액티비티
- `features/main/home`
  - 홈 화면, 알람 목록, 정렬/선택/삭제
- `features/main/alarmdetail`
  - 알람 생성/수정 화면
- `features/main/alarmringing`
  - 알람 울림 화면
- `routes/main`
  - Compose 내비게이션 루트
- `shared/alarm`
  - 알람 스케줄링, 브로드캐스트, 서비스, 권한 요구사항
- `shared/data`
  - Room DAO / Database / Repository
- `shared/model`
  - 도메인 모델 및 스케줄 계산 로직
- `shared/style`
  - 테마, 색상, 폰트 스케일 고정

### 구조적 특징

- 장점: 작고 명확한 앱에서는 기능 경계가 비교적 잘 보인다.
- 단점: Gradle 관점에서는 여전히 단일 모듈이므로 빌드 격리, 의존성 경계, 테스트 단위 분리가 약하다.
- 단점: `sourceSets`를 이용한 평면 구조는 Android Studio/Gradle 표준 구조와 조금 다르기 때문에 유지보수자가 처음 볼 때 진입 장벽이 있다.

## 4. 앱 실행 흐름

### 애플리케이션 시작

- `MyApplication`
  - Hilt 앱 엔트리
  - 폰트 스케일 고정 컨텍스트 적용
  - 알람 알림 채널 생성

### 메인 액티비티

- `MainActivity`
  - 폰트 스케일 1.0 고정
  - 잠금 화면 위 표시와 화면 켜기 동작 적용
  - Compose 루트로 `MainRoute()` 실행

### 내비게이션

`MainRoute`는 아래 화면을 관리한다.

- `home`
- `alarm/add`
- `alarm/edit/{alarmId}`
- `alarm/ringing/{alarmId}?label=...&ringDuration=...`

알람이 실제로 울리기 시작하면 서비스가 `AlarmStateHolder`를 갱신하고, `MainRoute`가 이를 관찰해 자동으로 `AlarmRingingScreen`으로 이동한다.

## 5. 핵심 기능 분석

### 5.1 홈 화면

`HomeScreen`은 알람 목록의 메인 진입점이다.

주요 기능:

- 다음 알람까지 남은 시간을 요약 표시
- 알람 목록 카드 UI
- 알람 on/off 토글
- 길게 눌러 다중 선택 진입
- 선택된 알람 일괄 켜기 / 삭제
- 정렬 방식 변경
  - 알람 시간 순
  - 생성 순 기반 수동 순서

구현 특징:

- `HomeViewModel`이 `Flow<List<Alarm>>`를 `StateFlow<HomeUiState>`로 변환
- `nextTriggerAtMillis()`를 이용해 홈 목록과 요약의 정렬 기준을 계산

제약:

- “직접설정한 순서”는 실제 드래그 재정렬이 아니라 `createdAt` 순이다.
- 목록이 커져도 문제는 크지 않지만, 현재는 페이징이나 고도화된 목록 최적화는 없다.

### 5.2 알람 생성/수정 화면

`AlarmDetailScreen`과 `AlarmDetailViewModel`이 담당한다.

설정 가능한 항목:

- 시 / 분
- 반복 요일
- 특정 날짜
- 알람 이름
- 알림음 URI
- 진동 on/off 및 진동 패턴
- 다시 울림 on/off
- 다시 울림 간격
- 다시 울림 횟수
- 울림 시간
  - `0초 = 계속 울림`
  - 5초~60초의 정규화된 선택값

저장 전 체크:

- exact alarm 권한 필요 여부
- 전체화면 알람 표시 권한 필요 여부
- 배터리 최적화 제외 필요 여부

추가 동작:

- 과거 특정 날짜를 선택하면 다음 가능한 시점으로 보정
- 중복 알람 저장 전 기존 동일 조건 알람 정리
- 라벨 길이 40자 제한

### 5.3 알람 울림 화면

`AlarmRingingScreen`과 `AlarmRingingViewModel`이 담당한다.

주요 기능:

- 현재 울리는 알람 라벨 표시
- 남은 울림 시간 표시
- 진행 바 표시
- 알람 해제 버튼

특징:

- 울림 시간이 `0`이면 계속 울림으로 간주
- 화면의 카운트다운 종료 또는 사용자의 해제 액션은 서비스 중지로 연결된다

## 6. 데이터 계층 분석

### Room

- 엔티티: `Alarm`
- DB 이름: `alarm_db`
- DB 버전: `3`

`Alarm` 모델은 아래 정보를 저장한다.

- 시간
- 반복 요일 문자열
- 라벨
- 활성화 여부
- 울림 지속 시간
- 알림음
- 특정 날짜
- 진동 여부 / 진동 모드
- 다시 울림 여부 / 간격 / 횟수
- 생성 시각

### DAO

`AlarmDao` 제공 기능:

- 전체 알람 조회
- ID 기준 조회
- 중복 알람 탐색
- 저장 / 수정 / 삭제

### Repository

`AlarmRepositoryImpl`은 단순 DB 래퍼가 아니라 스케줄러와 직접 연결된다.

즉:

- 알람 추가 시 DB 저장 후 스케줄 등록
- 알람 수정 시 기존 스케줄 취소 후 재등록
- 알람 삭제 시 스케줄 취소 후 DB 삭제
- 활성화 토글 시 스케줄 on/off 연동

이 구조는 앱의 핵심 도메인인 “저장과 스케줄 등록이 함께 움직여야 한다”는 요구에 맞다.

## 7. 알람 스케줄링 및 런타임 동작

이 프로젝트의 핵심 기술 포인트는 `shared/alarm` 패키지에 있다.

### 7.1 스케줄 등록

`AlarmScheduler`

- `AlarmManager` 사용
- 정확한 알람 전달을 위해 `setAlarmClock(...)` 사용
- 1회성 / 특정 날짜 / 반복 요일 알람 모두 지원
- 스누즈도 별도 exact alarm로 다시 등록

### 7.2 알람 전달

알람 발생 시 흐름:

1. `AlarmManager`가 `AlarmReceiver`를 호출
2. `AlarmReceiver`가 짧은 `WakeLock`을 확보
3. `AlarmForegroundService`를 시작
4. 서비스가 오디오 포커스를 요청하고, 소리 및 진동을 시작
5. `AlarmNotificationHelper`가 알림 및 full-screen intent를 표시
6. `AlarmStateHolder`가 UI 상태를 갱신
7. `MainRoute`가 `AlarmRingingScreen`으로 이동

### 7.3 알림 및 전체화면 표시

알람 알림 채널 특징:

- 중요도 `HIGH`
- DND 우회 요청
- 잠금 화면 공개 표시
- 전체화면 intent 사용

이는 일반 notification이 아니라, 잠금 화면에서도 사용자의 즉시 반응이 필요한 알람 UX를 의도한 구성이다.

### 7.4 서비스 동작

`AlarmForegroundService`는 아래 책임을 가진다.

- 포그라운드 서비스 시작
- 사운드 재생
- 진동 패턴 적용
- 오디오 포커스 요청 / 해제
- 스누즈 스케줄 등록
- 자동 정지 또는 계속 울림 처리

### 7.5 재부팅 대응

`BootReceiver`

- `BOOT_COMPLETED` 수신
- DB의 활성 알람 재조회
- 모든 활성 알람 재스케줄
- `goAsync()`로 브로드캐스트 생명주기 내 비동기 처리 안정성 보완

이 구조는 “재부팅 이후 Android가 기존 알람을 유지하지 않는다”는 플랫폼 제약을 정상적으로 보완한다.

## 8. 권한 및 시스템 제약 대응

Manifest 기준 주요 권한:

- `USE_EXACT_ALARM`
- `SCHEDULE_EXACT_ALARM` with `maxSdkVersion=32`
- `RECEIVE_BOOT_COMPLETED`
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
- `USE_FULL_SCREEN_INTENT`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `POST_NOTIFICATIONS`
- `VIBRATE`
- `WAKE_LOCK`

### 런타임 요구사항 헬퍼

`AlarmRuntimeRequirements`에서 아래를 체크한다.

- exact alarm 가능 여부
- 배터리 최적화 제외 여부
- full-screen intent 사용 가능 여부

### 정책 관점

- 현재 구조는 알람 앱이라는 전제를 두고 `USE_EXACT_ALARM` 경로를 사용 중이다.
- 이 앱의 첫 번째 주요 기능이 알람이라면 정책상 설명 가능성이 높다.
- 다만 Play Console exact alarm 선언은 별도로 정확히 작성해야 한다.

## 9. UI/디자인 특성

### 테마

- Accent Color: 강한 핑크 계열 `#FF0A54`
- 라이트/다크 모두 같은 포인트 컬러 사용
- 배경은 라이트에서는 밝은 회색, 다크에서는 거의 검정 계열

### 타이포그래피

- 기본 Typography 전체에 일괄적으로 `+4sp` 보정
- `Context`와 `Configuration` 모두 폰트 스케일을 `1.0f`로 고정

의도:

- 알람 앱 UI의 균일한 레이아웃 유지

트레이드오프:

- 사용자 접근성 설정의 글자 크기 확대를 무시하므로 접근성 측면에서는 불리하다

## 10. 빌드 및 배포 상태

### 현재 빌드 설정

- Java / Kotlin target: 17
- Compose 사용
- Release minify / shrink enabled
- release signing은 `key.properties` 기반
- 버전은 `gradle.properties`에서 관리

### 릴리즈 산출물

- AAB 빌드 명령: `./gradlew :app:bundleRelease`
- 가이드 문서: `PLAY_RELEASE.md`

### 배포 관점 체크포인트

- 현재 `applicationId`는 `com.seungmin.miram`
- exact alarm 사용 앱이므로 Play Console 권한 선언 필요
- 내부 테스트 → 비공개 테스트 → 프로덕션 순서가 적절

## 11. 강점

- 앱 목적과 구현 기술이 잘 맞는다
- `Room → Repository → Scheduler` 연결이 명확하다
- exact alarm, boot restore, foreground service, full-screen intent 등 알람 앱 핵심 요소가 반영되어 있다
- 홈/상세/울림 화면으로 기능 분리가 비교적 선명하다
- 권한과 시스템 설정 유도가 알람 저장 플로우에 통합되어 있다

## 12. 리스크 및 기술 부채

### 12.1 데이터 마이그레이션 전략

`Room.databaseBuilder(...).fallbackToDestructiveMigration()`을 사용하고 있다.

영향:

- 스키마 변경 시 기존 사용자 알람이 삭제될 수 있다
- 출시 후에는 명시적 migration으로 바꾸는 것이 안전하다

### 12.2 테스트 부재

저장소에서 단위 테스트나 UI 테스트 디렉토리를 확인하지 못했다.

영향:

- 알람 시간 계산
- 재부팅 후 재등록
- 스누즈 반복 횟수
- 권한 체크 플로우

같은 핵심 기능이 회귀에 취약할 수 있다.

### 12.3 구조의 비표준성

루트 폴더 기반 flat sourceSet 구조는 초기에 빠르게 만들기엔 편하지만, 아래 문제가 있다.

- 일반적인 Android 프로젝트 기대 구조와 다름
- 파일 이동/모듈화 시 비용 증가
- IDE와 신규 기여자 온보딩 비용 증가

### 12.4 접근성

폰트 스케일을 강제로 1.0으로 고정하는 방식은 UX 일관성은 좋지만 접근성 설정을 존중하지 못한다.

### 12.5 수동 정렬 표현

홈 화면의 “직접설정한 순서”는 실제 사용자 지정 순서가 아니라 `createdAt` 순서다. 라벨과 실제 동작 사이에 간극이 있다.

### 12.6 정책 의존성

`USE_EXACT_ALARM` 사용은 알람 앱으로서 타당해 보이지만, Google Play 정책 설명과 권한 선언이 잘못되면 심사 리스크가 생긴다.

## 13. 추천 개선 사항

### 단기

- `PROJECT_ANALYSIS.md`를 기준으로 기능/정책 문서 정리
- Room migration 명시적 추가
- 알람 시간 계산 로직 테스트 추가
- “직접설정한 순서” 문구 수정 또는 실제 재정렬 기능 구현
- 권한/설정 유도 플로우를 한 화면 상태 모델로 정리

### 중기

- `features`, `domain`, `data`, `alarm-runtime` 수준의 실모듈 분리 검토
- OEM 절전 정책 대응 가이드 화면 추가
- 알람 울림 화면 접근성 및 잠금 화면 UX 점검

### 장기

- 릴리즈 준비 문서와 코드 상태를 연결한 운영 체크리스트 작성
- analytics / crash reporting / user diagnostics 추가 검토

## 14. 요약

MiRam AOS는 작은 규모지만 핵심이 분명한 알람 앱이다. 현재 구조는 “정확한 시간에 울리는 알람”이라는 목표에 잘 맞게 짜여 있으며, 스케줄 등록부터 울림 처리까지 흐름이 명확하다. 특히 exact alarm, foreground service, boot restore, full-screen intent까지 포함하고 있어 기능 축은 적절하다.

반면 출시 품질 관점에서는 마이그레이션, 테스트, 정책 문서화, 구조 표준화, 접근성 대응이 아직 보강 포인트다. 즉, 프로토타입을 넘어서 실사용 배포를 준비하는 단계에 가까운 코드베이스로 보는 것이 정확하다.
