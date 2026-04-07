# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 응답 언어

항상 한국어로 대답한다.

## Build & Development Commands

```bash
# 빌드
./gradlew assembleDebug

# 설치 (연결된 기기/에뮬레이터)
./gradlew installDebug

# 전체 테스트
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.wbjang.footballdiary.ExampleUnitTest"

# 린트
./gradlew lint

# 클린 빌드
./gradlew clean assembleDebug
```

## API 키 설정

`local.properties`에 football-data.org API 키를 추가해야 빌드 시 `BuildConfig.FOOTBALL_API_KEY`로 주입된다:

```properties
football_api_key=YOUR_KEY_HERE
```

## 아키텍처

Clean Architecture (UI → Domain → Data), 단방향 데이터 흐름.

```
ui/          Composables + HiltViewModels (StateFlow 구독)
domain/      Repository 인터페이스 + 순수 Kotlin 모델
data/        Repository 구현체 (API + Room + DataStore)
di/          Hilt 모듈 3개
```

### 네비게이션 구조

두 개의 NavHost가 중첩되어 있다:

- **`NavGraph.kt`** (최상위): `Screen.Onboarding` → `Screen.Main` (팀 선택 후 popUpTo로 스택 제거)
- **`MainScreen.kt`** (내부): BottomNavigation 3탭 + 탭 위에 쌓이는 추가 라우트

```
NavGraph
└── MainScreen (하단 탭 NavHost)
    ├── "schedule"     → ScheduleScreen
    ├── "diary"        → DiaryScreen
    ├── "settings"     → SettingsScreen
    ├── "matchDetail"  → MatchDetailScreen  (하단 바 숨김)
    └── "writeReview"  → WriteReviewScreen  (하단 바 숨김)
```

`showBottomBar`는 현재 라우트가 `matchDetail` 또는 `writeReview`가 아닐 때 true.

### 상태 공유 패턴

`MainViewModel`이 탭 간 공유 상태를 보유한다:
- `selectedMatch` — ScheduleScreen에서 선택한 경기 (MatchDetailScreen으로 전달)
- `selectedMatchDetail` — MatchDetailViewModel이 로드한 detail (WriteReviewScreen으로 전달)

MatchDetailScreen에서 detail이 로드되면 `MainScreen`의 `LaunchedEffect`가 `viewModel.selectMatchDetail(it)`를 호출해 MainViewModel로 올려보낸다.

### 데이터 레이어

**FootballRepository** 인터페이스 하나가 모든 데이터 접근을 추상화한다. `FootballRepositoryImpl`이 구현:
- 네트워크: `FootballApiService` (Retrofit, football-data.org v4 API, `X-Auth-Token` 헤더)
- 로컬 DB: `ReviewDao` (Room, `football_diary.db`) — 경기 소감 저장
- 설정: `UserPreferencesDataStore` — 팔로잉 팀 정보 (ID, 이름, 크레스트 URL)

`ReviewEntity`의 `emotionTags`는 쉼표로 구분된 문자열로 저장되며, `toDomain()`에서 `List<String>`으로 변환된다.

### API 샘플 데이터 처리

football-data.org 무료 플랜은 경기 상세 데이터 일부를 제공하지 않는다. `MatchDetailViewModel`이 API 응답이 없는 섹션을 `SampleMatchData`로 대체하고, `sampleSections: Set<SampleSection>`으로 어떤 섹션이 샘플인지 추적해 UI에서 뱃지를 표시한다.

## 리소스 규칙

모든 문자열, 치수, 색상은 `res/` 폴더에 정의한다. Composable 내부에 리터럴 값을 하드코딩하지 않는다:
- 문자열 → `res/values/strings.xml` + `stringResource()`
- 치수 → `res/values/dimens.xml` + `dimensionResource()`
- 색상 → `ui/theme/Color.kt` + `MaterialTheme.colorScheme.*`
