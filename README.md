<div align="center">

<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" alt="Football Diary Icon"/>

# Football Diary

> 응원하는 축구팀의 경기 일정을 확인하고, 관전 후 감상을 기록하는 다이어리 앱

<br>

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![minSdk](https://img.shields.io/badge/minSdk-26-brightgreen?style=flat-square)
![targetSdk](https://img.shields.io/badge/targetSdk-34-brightgreen?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

</div>

<br>

---

## 📱 스크린샷

> GIF 또는 스크린샷을 아래 표에 삽입해주세요

| 온보딩 | 경기 일정 | 경기 상세 | 소감 작성 | 위젯 |
|:---:|:---:|:---:|:---:|:---:|
| <img width="160" src="docs/screenshot_onboarding.png"> | <img width="160" src="docs/screenshot_schedule.png"> | <img width="160" src="docs/screenshot_detail.png"> | <img width="160" src="docs/screenshot_diary.png"> | <img width="160" src="docs/screenshot_widget.png"> |
| 팀 선택 | 캘린더/리스트 | 라인업·통계 | 평점·태그·소감 | 홈 화면 |

<br>

---

## ✨ 주요 기능

- ⚽ **팀 팔로우** — 응원 팀 설정 및 변경, 팔로잉 팀 경기 우선 표시
- 📅 **경기 일정 조회** — EPL·UCL 등 주요 리그 일정 및 결과 확인
- 📊 **경기 상세** — 라인업 포메이션 시각화, 타임라인, 경기 통계, 리그 순위표
- 📝 **소감 기록** — 경기별 별점·감정 태그·MOM·텍스트 소감 작성 및 관리
- 🔔 **경기 알림** — 경기 시작 15분 전 알림 (Android 12+ 정확한 알람 권한 처리)
- 🏠 **홈 화면 위젯** — 팔로잉 팀의 다음 경기 정보를 홈 화면에서 확인
- 🌓 **테마** — 다크 / 라이트 / 시스템 설정 연동

<br>

---

## ⭐ 기술적 하이라이트

### 아키텍처

- **Clean Architecture** — UI → Domain → Data 단방향 의존성
- **MVVM + StateFlow** 기반 단방향 데이터 흐름
- **Repository 패턴** — Remote(Retrofit)와 Local(Room) 데이터 소스 추상화

```
┌─────────────────────────────────┐
│  UI Layer                       │
│  Compose Screen + ViewModel     │
├─────────────────────────────────┤
│  Domain Layer                   │
│  Repository Interface           │
├─────────────────────────────────┤
│  Data Layer                     │
│  ├─ Retrofit  (football-data.org│
│  ├─ Room      (소감 로컬 저장)  │
│  └─ DataStore (사용자 설정)     │
└─────────────────────────────────┘
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

## 🔥 트러블슈팅

### 1. HiltWorker ClassNotFoundException

**문제** — WorkManager 실행 시 `MatchNotificationWorker`를 찾지 못하는 에러

```
E WM-WorkerFactory: java.lang.ClassNotFoundException:
  com.wbjang.footballdiary.notification.MatchNotificationWorker
```

**원인** — Hilt Worker 사용 시 `App.kt`에서 `Configuration.Provider` 미구현으로
기본 WorkManager 초기화와 충돌

**해결**
```kotlin
@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```
AndroidManifest.xml에서 기본 WorkManager `InitializationProvider` 제거 병행

---

### 2. Glance Widget — cornerRadius 미지원

**문제** — 위젯 UI 구성 중 `GlanceModifier.cornerRadius()` 컴파일 에러

**원인** — Glance는 Android RemoteViews 기반이라 일반 Compose Modifier API와 다름.
`cornerRadius`, `clip` 등 일부 Modifier 미지원

**해결** — drawable XML 또는 유니코드 문자로 대체
```kotlin
// cornerRadius 대신 유니코드 bullet 문자 활용
Text(
    text = "● ${matchTime}",
    style = TextStyle(color = textMuted, fontSize = 9.sp)
)
```

---

### 3. 포메이션 기반 라인업 배치

**문제** — API 응답의 선수 포지션이 실제 전술 포지션이 아닌 주 포지션으로 제공됨
(4-2-3-1에서 오른쪽에 뛴 산초도 `Left Winger`로 반환)

**원인** — football-data.org API 스펙상 포지션은 선수 고유 포지션 기준

**해결** — 포지션 문자열 대신 포메이션 문자열을 파싱해 라인업 배열 순서대로 슬롯 배치
```kotlin
fun buildFormationSlots(formation: String, lineup: List<Player>): List<List<Player>> {
    val lines = formation.split("-").map { it.toInt() }
    var index = 1
    val result = mutableListOf(listOf(lineup[0])) // GK
    lines.forEach { count ->
        result.add(lineup.subList(index, index + count))
        index += count
    }
    return result
}
```

---

### 4. Android 12+ 정확한 알람 권한

**문제** — Android 12(API 31) 이상에서 정확한 알람 설정 시 크래시

**원인** — `SCHEDULE_EXACT_ALARM` 권한이 런타임에 별도로 필요

**해결** — `canScheduleExactAlarms()` 분기 처리 및 권한 요청 UI 추가
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val alarmManager = context.getSystemService(AlarmManager::class.java)
    if (!alarmManager.canScheduleExactAlarms()) {
        // 설정 화면으로 이동하여 권한 요청
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).also {
            context.startActivity(it)
        }
    }
}
```

---

### 5. 부팅 후 알람 소실

**문제** — 기기 재시작 후 예약된 경기 알림이 모두 사라짐

**원인** — AlarmManager 알람은 기기 재부팅 시 초기화됨

**해결** — `BOOT_COMPLETED` BroadcastReceiver에서 WorkManager로 알람 재등록
```kotlin
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            MatchNotificationScheduler.rescheduleAll(context)
        }
    }
}
```

<br>

---

## 🗂 프로젝트 구조

```
📦 com.wbjang.footballdiary
 ├── 📂 di                 # Hilt 모듈 (NetworkModule, RepositoryModule, WorkerModule)
 ├── 📂 network            # Retrofit API Service, AuthInterceptor
 ├── 📂 model              # 데이터 클래스 (Match, Score, Player, Diary 등)
 ├── 📂 repository         # Repository 구현체
 ├── 📂 viewmodel          # ViewModel (Match, Diary, Settings)
 ├── 📂 screen             # Composable 화면
 │   └── 📂 component      # 재사용 컴포넌트 (MatchCard, DiaryCard, FormationView 등)
 ├── 📂 notification       # WorkManager Worker, AlarmManager, BootReceiver
 ├── 📂 widget             # Glance AppWidget, WidgetUpdateWorker
 ├── 📂 local              # Room Database, DAO, DataStore
 ├── 📂 ui/theme           # 컬러 시스템, Typography, Theme
 └── 📂 util               # DateUtil, Constants, Extensions
```

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

---

## 🚀 빌드 방법

**1. 저장소 클론**
```bash
git clone https://github.com/wbjang/football-diary.git
cd football-diary
```

**2. API 토큰 발급**

[football-data.org](https://www.football-data.org/) 가입 후 이메일로 토큰 수령

**3. local.properties 설정**
```properties
FOOTBALL_API_TOKEN=여기에_발급받은_토큰_입력
```

**4. 실행**

Android Studio에서 Run (Min SDK 26 / Android 8.0 이상)

<br>

---

## 🔮 향후 개선 사항

- [ ] 단위 테스트 / UI 테스트 커버리지 확대
- [ ] 여러 팀 동시 팔로우
- [ ] 소감 통계 화면 (별점 분포, 감정 태그 분석)
- [ ] Play Store 배포
- [ ] Compose Multiplatform 마이그레이션 검토

<br>

---

## 👨‍💻 개발자

| | |
|---|---|
| GitHub | [@wbjang](https://github.com/wbjang) |
| Email | 이메일 주소 입력 |
| Blog | 블로그 주소 입력 |

<br>

---

## 📄 License

```
MIT License

Copyright (c) 2024 wbjang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction.
```
