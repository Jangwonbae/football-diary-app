<div align="center">

<img width="100%" alt="Football Diary" src="https://github.com/user-attachments/assets/d70fcff6-5361-4c02-8058-e3cb6703058f" />

</div>

<br>

## ✨ 주요 기능

- ⚽ **팀 팔로우** — 응원 팀 설정 및 변경, 팔로잉 팀 경기 표시
- 📅 **경기 일정 조회** — EPL·UCL 등 주요 리그 일정 및 결과, 리그 순위표 확인
- 📊 **경기 상세** — 라인업 포메이션 시각화, 타임라인, 경기 통계
- 📝 **소감 기록** — 경기별 별점·감정 태그·텍스트 소감 작성 및 관리
- 🔔 **경기 알림** — 경기 시작 15분 전 알림
- 🏠 **홈 화면 위젯** — 팔로잉 팀의 다음 경기 정보를 홈 화면에서 확인
- 🌓 **테마** — 다크 / 라이트 / 시스템 설정 연동

<br>

---

## ⭐ 기술적 하이라이트

### 아키텍처

- **Clean Architecture** — UI / Domain / Data 3계층 분리 + 의존성 역전 
- **MVVM + StateFlow** — 단방향 데이터 흐름(UDF)과 상태 기반 UI
- **Repository 패턴** — Remote(Retrofit) / Local(Room) 데이터 소스 추상화  

```
┌────────────────────────────────┐
│  UI Layer                      │
│  Compose Screen + ViewModel    │
├────────────────────────────────┤
│  Domain Layer                  │
│  Repository Interface          │
├────────────────────────────────┤
│  Data Layer                    │
│  ├─ Retrofit  football-data.org│
│  ├─ Room      소감 로컬 저장    │
│  └─ DataStore 사용자 설정       │
└────────────────────────────────┘
```

### Modern Android Stack

| 분류 | 기술 | 선택 이유 |
|---|---|---|
| UI | Jetpack Compose + Material 3 | 선언형 UI로 상태 관리 단순화 |
| DI | Hilt | 보일러플레이트 최소화, 테스트 용이 |
| Async | Coroutines + Flow | 구조화된 동시성, 생명주기 안전 |
| Network | Retrofit2 + OkHttp3 | 타입 안전 API 호출, Interceptor 확장 |
| Local DB | Room | SQLite 추상화, Flow 지원 |
| Settings | DataStore | SharedPreferences 대비 타입 안전 |

### 플랫폼 기능 활용

- **Jetpack Glance** — Compose 문법으로 홈 화면 위젯 구현
- **WorkManager** — 6시간 주기 경기 일정 동기화, 기기 재시작 후에도 유지
- **AlarmManager** — Android 12+ `SCHEDULE_EXACT_ALARM` 권한 분기 처리
- **BroadcastReceiver** — 부팅 후 알람 재등록 (`BOOT_COMPLETED`)
- **SplashScreen API** — 시스템 스플래시와 통합, 깜빡임 없는 초기 화면

<br>

---

## 🗂 프로젝트 구조

```
📦 com.wbjang.footballdiary
│
├── 📱 MainActivity.kt                       # 앱 진입점 (Compose Host)
├── 🏗️ FootballDiaryApplication.kt          # Hilt + WorkManager 초기화
│
├── ui/                                      # 🎨 Presentation Layer
│   ├── navigation/
│   │   ├── NavGraph.kt                      # 최상위 네비게이션 그래프
│   │   └── Screen.kt                        # 라우트 정의
│   ├── onboarding/                          # 팀 선택 화면
│   │   ├── OnboardingScreen.kt
│   │   ├── OnboardingViewModel.kt
│   │   └── LeagueDisplay.kt
│   ├── main/                                # 메인 탭 화면
│   │   ├── MainScreen.kt                    # BottomNav + 중첩 NavHost
│   │   ├── MainViewModel.kt                 # 탭 간 공유 상태
│   │   ├── schedule/                        # 📅 일정 탭
│   │   │   ├── ScheduleScreen.kt
│   │   │   ├── ScheduleViewModel.kt
│   │   │   ├── MatchDetailScreen.kt         # 경기 상세 + 순위표
│   │   │   ├── MatchDetailViewModel.kt
│   │   │   └── SampleMatchData.kt           # API 미제공 데이터 fallback
│   │   ├── diary/                           # 📝 다이어리 탭
│   │   │   ├── DiaryScreen.kt
│   │   │   ├── DiaryViewModel.kt
│   │   │   ├── WriteReviewScreen.kt
│   │   │   └── WriteReviewViewModel.kt
│   │   └── settings/                        # ⚙️ 설정 탭
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   ├── components/                          # 재사용 Composable
│   │   └── ExpandableTagRow.kt
│   └── theme/                               # Material 3 테마
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── domain/                                  # 🎯 Domain Layer
│   ├── model/                               # 순수 Kotlin 도메인 모델
│   │   ├── Match.kt                         # 경기 (상태 enum 포함)
│   │   ├── MatchDetail.kt                   # 경기 상세
│   │   ├── Review.kt                        # 소감 기록
│   │   ├── Team.kt / League.kt / Standing.kt
│   │   └── ThemeMode.kt
│   └── repository/
│       └── FootballRepository.kt            # Repository 추상 인터페이스
│
├── data/                                    # 💾 Data Layer
│   ├── api/                                 # 네트워크
│   │   ├── FootballApiService.kt            # Retrofit 인터페이스
│   │   └── dto/                             # API 응답 DTO
│   │       ├── CompetitionTeamsResponse.kt
│   │       ├── MatchDetailResponse.kt
│   │       ├── StandingsResponse.kt
│   │       └── TeamMatchesResponse.kt
│   ├── local/                               # 로컬 DB (Room)
│   │   ├── AppDatabase.kt
│   │   ├── dao/ReviewDao.kt
│   │   └── entity/ReviewEntity.kt
│   ├── datastore/
│   │   └── UserPreferencesDataStore.kt      # 팔로우 팀, 테마, 알림 설정
│   ├── mapper/
│   │   └── MatchMapper.kt                   # DTO ↔ Domain 변환
│   └── repository/
│       └── FootballRepositoryImpl.kt        # Repository 구현체
│
├── di/                                      # 💉 Hilt DI Modules
│   ├── AppModule.kt
│   ├── NetworkModule.kt                     # Retrofit / OkHttp 설정
│   └── DatabaseModule.kt                    # Room 설정
│
├── notification/                            # 🔔 알림 + 백그라운드
│   ├── NotificationScheduler.kt             # AlarmManager 래퍼
│   ├── MatchNotificationReceiver.kt         # 알람 수신 → 알림 발송
│   ├── BootReceiver.kt                      # 부팅 후 알람 재등록
│   └── MatchScheduleSyncWorker.kt           # 6시간 주기 일정 동기화
│
├── widget/                                  # 🏠 홈 화면 위젯 (Glance)
│   ├── MatchWidget.kt                       # Compose 기반 위젯 UI
│   ├── MatchWidgetReceiver.kt               # AppWidgetProvider
│   ├── WidgetScheduler.kt                   # 위젯 갱신 예약
│   ├── WidgetUpdateWorker.kt                # WorkManager 워커
│   ├── WidgetPreferences.kt                 # 위젯 전용 저장소
│   └── WidgetMatch.kt                       # 위젯용 데이터 모델
│
└── util/
    └── AppLogger.kt                         # 통합 로깅 유틸
```

### 레이어 간 의존 방향

```
ui ──→ domain ←── data
         ↑
         └── (interface 정의)
             data가 구현체 제공 (DI로 주입)
```

- **ui** 는 domain에만 의존 — 데이터 소스 교체 용이
- **domain** 은 순수 Kotlin — Android 프레임워크 의존 없음
- **data** 는 domain 인터페이스 구현 — Retrofit / Room 등 세부 구현 은닉
<br>

---

## 📡 사용 API

[football-data.org](https://www.football-data.org/) v4 — 무료 플랜

| 엔드포인트 | 설명 |
|---|---|
| `GET /matches` | 오늘 경기 목록 |
| `GET /matches/{id}` | 경기 상세 (라인업 · 통계 · 타임라인) |
| `GET /competitions/{id}/matches` | 리그 경기 일정 |
| `GET /competitions/{id}/standings` | 리그 순위표 |
| `GET /teams/{id}/matches` | 팀 경기 목록 |

> ⚠️ API 토큰은 `local.properties`에서 관리하며 저장소에 포함되지 않습니다.

<br>

## 📱 화면 구성


### 👋 온보딩

앱 최초 진입 시 팔로우할 팀을 선택하는 화면. 선택 완료 후 메인 화면으로 이동하며 백스택에서 제거된다.

`5개 리그 지원` `펼치기/접기 토글` `확인 다이얼로그` `DataStore 저장` `알림·위젯 즉시 갱신`

<img width="320" src="https://github.com/user-attachments/assets/d43ec2f3-d797-47c6-b125-64791797fc41" />

---

### 📅 매치 일정

팔로우 팀의 시즌 경기 일정과 리그 순위를 확인하는 화면. 리스트·캘린더 뷰를 자유롭게 전환할 수 있으며, 다가오는 경기로 자동 스크롤된다.

`리스트/캘린더 뷰 토글` `리그 순위표` `소감 작성 여부 뱃지` `월 단위 스와이프` `다가오는 경기 자동 스크롤`

<img width="320" src="https://github.com/user-attachments/assets/25ca4544-d583-4326-804b-6b6167f012d7" />

---

### 📊 매치 상세

선택한 경기의 라인업·타임라인·통계를 한 곳에서 확인하는 화면. 무료 API 미제공 데이터는 샘플 뱃지로 명시한다.

`선발 라인업` `득점·경고·교체 타임라인` `경기 통계` `샘플 데이터 뱃지` `소감 연동 표시`

<img width="320" src="https://github.com/user-attachments/assets/642906d0-4428-421d-9464-fac7dec0e026" />

---

### 📝 매치 작성 & 매치 다이어리

경기별 별점·감정 태그·텍스트 소감을 작성·수정하는 화면. 기존 리뷰가 있으면 자동으로 프리필되며, 시즌별로 목록을 열람할 수 있다.

`1~5점 별점` `감정 태그 토글` `커스텀 태그 입력` `신규/수정 모드` `시즌 필터 & 정렬` `스코어 자동 보정`

<img width="320" src="https://github.com/user-attachments/assets/d3b7f568-8b4c-4df5-a532-4a2f75d1badd" />

---

### 🏠 홈 화면 위젯

팔로잉 팀의 다음 경기를 홈 화면에서 바로 확인할 수 있는 Glance 기반 위젯. 갱신 시 WorkManager로 네트워크 요청을 수행해 최신 경기 정보를 가져온 뒤 위젯 데이터를 업데이트 한다.

`Jetpack Glance` `WorkManager 주기 갱신` `탭 시 앱 진입`

<img width="320" src="https://github.com/user-attachments/assets/770e2ba3-bcc8-46d6-b5ab-8b57da4fd1d0" />

---


### 🌓 테마

라이트·다크·시스템 설정 연동을 지원한다.

`라이트 모드` `다크 모드` `시스템 설정 연동`

<img width="320" height="640" alt="다크테마_360w" src="https://github.com/user-attachments/assets/f3a92bc5-6701-48e3-9aac-c9611f15e02e" />

---

## 👨‍💻 개발자

| | |
|---|---|
| GitHub | [@Jangwonbae](https://github.com/Jangwonbae) |
| Email | wonbae623@naver.com |

