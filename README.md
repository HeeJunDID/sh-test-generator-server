# 테스트케이스 자동생성 도구 — 서버

요구사항을 입력받아 AI로 QA 테스트케이스를 자동 생성하는 Spring Boot 백엔드 서버입니다.

## 기술 스택

- **Java 21**
- **Spring Boot 3.3.0**
- **Spring Security 6** + **JWT** (jjwt 0.12.x)
- **Spring Data JPA** + **H2** (파일 모드, 재시작 후에도 데이터 유지)
- **Gradle 8.14**

## 아키텍처

```
Controller → Service → AiProviderRouter → AiProvider (Dify / Gemini / Claude)
                ↓
         GenerationHistory (H2 저장)
```

- **AiProvider** 인터페이스로 제공자 추상화, 사용자별 `preferredAiProvider`로 동적 라우팅
- **Dify** 워크플로우 → 기본 케이스 생성, **Gemini** 보강 → 전제조건 / 단계 / 기대 결과 추가
- **ADMIN / USER** 역할 분리, `/api/admin/**` 는 ADMIN만 접근 가능

## 프로젝트 구조

```
src/main/java/com/testcasegenerator/
├── config/              # SecurityConfig (CORS, JWT 필터), RestClientConfig
├── common/              # ApiResponse, BusinessException, GlobalExceptionHandler
├── controller/          # TestCaseController, AuthController, HistoryController, UserController, AdminController
├── service/             # 비즈니스 로직
├── domain/              # JPA 엔티티 (User, GenerationHistory, AdminTestCase)
├── dto/                 # Request / Response DTO
├── security/            # JwtUtil, JwtAuthFilter
└── infrastructure/ai/
    ├── AiProvider.java         # 인터페이스
    ├── AiProviderRouter.java   # 동적 라우팅
    ├── dify/                   # Dify 워크플로우 + Gemini 보강
    ├── gemini/                 # Gemini 단독
    └── claude/                 # Claude
```

## 실행 방법

```bash
export GEMINI_API_KEY=...
export DIFY_API_KEY=...          # 선택
export ANTHROPIC_API_KEY=...     # 선택

./gradlew bootRun
```

서버는 **http://localhost:8080** 에서 실행됩니다.  
H2 콘솔: **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:file:./data/testcase-history`)

## 기본 계정

| 아이디 | 비밀번호 | 역할 |
|--------|----------|------|
| admin | admin | ADMIN |

## API

### 인증

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/auth/login` | 로그인, JWT 토큰 반환 |

### 테스트케이스

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/generate` | 요구사항 입력 → 테스트케이스 생성 |

**Request**
```json
{
  "title": "회원 로그인 기능",
  "description": "이메일과 비밀번호로 로그인하는 화면",
  "devCategory": "screen",
  "isNew": "new",
  "dbWork": "target",
  "monetary": "no"
}
```

| 필드 | 값 |
|------|-----|
| `devCategory` | `screen` \| `online` \| `batch` |
| `isNew` | `new` \| `existing` |
| `dbWork` | `target` \| `non-target` |
| `monetary` | `yes` \| `no` |

### 이력

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/history` | 내 생성 이력 목록 |
| GET | `/api/history/{id}` | 이력 상세 (테스트케이스 포함) |

### 사용자 설정

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/users/me` | 내 정보 조회 |
| PUT | `/api/users/me/settings` | AI 제공자 설정 변경 |

### 어드민 (ADMIN 역할 필요)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/admin/testcases` | 전체 테스트케이스 목록 |
| POST | `/api/admin/testcases` | 테스트케이스 1건 등록 |
| POST | `/api/admin/testcases/bulk` | 엑셀 파싱 결과 일괄 등록 |
| DELETE | `/api/admin/testcases/{id}` | 테스트케이스 삭제 |

## AI 제공자

| 제공자 | 환경변수 | 설명 |
|--------|----------|------|
| `dify` | `DIFY_API_KEY` | Dify 워크플로우 + Gemini 보강 (기본값) |
| `gemini` | `GEMINI_API_KEY` | Gemini 단독 |
| `claude` | `ANTHROPIC_API_KEY` | Claude 단독 |

`application.yml`의 `app.ai.provider` 또는 사용자별 `preferredAiProvider`로 제어합니다.

## 배포

`main` 브랜치 push 시 GitHub Actions가 자동으로 ECR 빌드 → EC2 배포합니다.

- **프로덕션**: `http://43.202.110.99:8080` (내부), 클라이언트 nginx 프록시로 외부 노출
- **빌드**: `amazoncorretto:21-alpine` 멀티스테이지, arm64 (t4g.large)
- **환경변수**: EC2 `docker-compose.yml`에서 주입 (`GEMINI_API_KEY`, `DIFY_API_KEY` 등)
