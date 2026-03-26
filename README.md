# sh-test-generator-server

테스트케이스 자동생성 도구의 백엔드 서버입니다.
요구사항을 입력받아 Claude AI를 통해 QA 테스트케이스를 자동으로 생성합니다.

## 기술 스택

- **Java 21** (런타임)
- **Spring Boot 3.3.0**
- **Gradle 8.14**
- **Anthropic Claude API** (테스트케이스 생성)

## 아키텍처

Layered Architecture + AI Provider 추상화

```
Controller → Service → Infrastructure (AiProvider)
                              ↓
                    ClaudeAiProvider (확장 시 OpenAI 등 교체 가능)
```

```
src/main/java/com/testcasegenerator/
├── config/                  # CORS, RestClient 설정
├── common/                  # ApiResponse, BusinessException, GlobalExceptionHandler
├── controller/              # REST API 엔드포인트
├── service/                 # 비즈니스 로직
├── dto/                     # Request / Response DTO
└── infrastructure/ai/       # AI 제공자 추상화 및 Claude 구현체
```

## 사전 요구사항

- Java 21
- Anthropic API Key ([console.anthropic.com](https://console.anthropic.com))

## 실행 방법

```bash
# API 키 환경변수 설정
export ANTHROPIC_API_KEY=sk-ant-...

# 서버 실행 (Java 21 사용)
JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./gradlew bootRun
```

서버는 **http://localhost:8080** 에서 실행됩니다.

## API

### POST /api/generate

요구사항을 입력받아 테스트케이스를 생성합니다.

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

| 필드 | 타입 | 값 |
|------|------|----|
| `devCategory` | string | `screen` \| `online` \| `batch` |
| `isNew` | string | `new` \| `existing` |
| `dbWork` | string | `target` \| `non-target` |
| `monetary` | string | `yes` \| `no` |

**Response**
```json
{
  "success": true,
  "data": [
    {
      "id": "TC-001",
      "programName": "SCR_LOGIN_01",
      "testData": "유효한 이메일과 비밀번호",
      "title": "정상 로그인",
      "precondition": "사용자가 가입되어 있음",
      "steps": ["1. 이메일 입력", "2. 비밀번호 입력", "3. 로그인 버튼 클릭"],
      "expected": "메인 화면으로 이동",
      "priority": "high",
      "category": "신규기능"
    }
  ]
}
```

**category 종류:** `신규기능` | `수정기능` | `예외처리` | `성능` | `경계값`
**priority 종류:** `high` | `medium` | `low`

## 테스트 실행

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@21 ANTHROPIC_API_KEY=test ./gradlew test
```

## 프론트엔드 연동

[TestCaseGenerator](https://github.com/HeeJunDID/TestCaseGenerator) 프론트엔드의 `App.vue`에서 `generateSampleTestCases()` 함수를 아래와 같이 교체하세요.

```javascript
async function handleGenerate(form) {
  const response = await fetch('http://localhost:8080/api/generate', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(form)
  })
  const result = await response.json()
  testCases.value = result.data
  selectedTestCase.value = testCases.value[0] ?? null
}
```
