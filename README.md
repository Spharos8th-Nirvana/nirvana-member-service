# nirvana-member-service

## 서비스 개요

- 목적: 회원 도메인 기능을 담당한다.
- 담당자 / 백업 리뷰어: 미정 — 팀 확인 후 기입
- 소유 데이터와 운영 책임: 세부 데이터 소유 범위와 운영 책임은 팀 확인 후 기입
- API 문서: [Swagger UI](http://localhost:8082/swagger-ui/index.html)
- 기본 포트 / 헬스체크: `8082` (임시) / `GET /actuator/health`

Auth 회원가입 요청에 필요한 일반 회원 프로필 등록을 제공합니다.
포트는 임시값이며 담당자·백업 리뷰어·세부 데이터 소유 범위는 미정입니다.
로컬 실행에는 JDK 21과 초기화된 MySQL 스키마 `nirvana_member`가 필요합니다.
헬스 체크: http://localhost:8082/actuator/health
Config Repo 연결 시 서비스 설정 파일 이름: `member-service.yml` (파일 생성·연동은 별도 작업)

## 서비스 레포 생성 후

[서비스 초기 설정 가이드](docs/INITIAL_SETUP.md)에 따라 다음 항목을 변경합니다.

- Gradle 프로젝트 이름
- Java 패키지
- 시작 클래스 이름
- 애플리케이션 이름
- 실행 포트
- README의 서비스 정보

애플리케이션 이름은 Config Repo의 서비스별 설정 파일 이름과 일치시킵니다.
예시값을 그대로 적용하지 말고 담당 서비스의 이름과 포트를 확인하세요.

## 로컬 실행

- 사전 요구사항은 JDK 21입니다.
- macOS와 Linux: `./gradlew bootRun`
- Windows PowerShell: `.\gradlew.bat bootRun`
- Windows Git Bash: `./gradlew bootRun`
- 기본 local 프로필은 Config Server 없이 시작하며 Swagger UI를 활성화합니다.
- 다른 프로필에서는 API 문서와 Swagger UI가 기본으로 비활성화됩니다.

### Config Server 사용

- `SPRING_PROFILES_ACTIVE=config-server`로 설정합니다.
- 로컬 Swagger UI도 함께 사용하려면 `local,config-server`로 설정합니다.
- `CONFIG_SERVER_URL` 기본값은 `http://localhost:8888`입니다.

## 일반 회원 프로필 생성 API

Auth는 `POST /internal/profiles/{userUuid}`를 호출하고 `X-Member-Service-Token` 헤더를 전달합니다. 토큰은 Member의 `MEMBER_ACCESS_AUTH_SECRET`과 일치해야 합니다. 본문에는 `name`, `nickname`, 선택 항목 `countryCode`를 JSON으로 보냅니다. 생성에 성공하면 같은 UUID의 프로필과 함께 `201 Created`를 반환합니다.

`GET /internal/profiles/{userUuid}`는 동일한 서비스 토큰을 사용해 활성 프로필을 조회합니다. 삭제된 프로필이나 존재하지 않는 UUID에는 `404 PROFILE_NOT_FOUND`를 반환합니다.

MySQL 접속은 `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`로 설정합니다. 기본 주소는 `127.0.0.1:13306`입니다.

## 빌드·테스트

macOS, Linux, Windows Git Bash:

```bash
./gradlew build
```

Windows PowerShell:

```powershell
.\gradlew.bat build
```

JAR만 빌드하려면 bootJar를 사용합니다.
develop·main 대상 PR의 CI는 빌드, 테스트, REST Docs 생성을 실행합니다.

### API 문서

- local 프로필에서 Swagger UI는 `http://localhost:8082/swagger-ui/index.html`, OpenAPI JSON은 `http://localhost:8082/v3/api-docs`에서 확인할 수 있습니다.
- `./gradlew restDocs` 또는 Windows에서 `.\gradlew.bat restDocs`를 실행하면 테스트를 거쳐 `build/docs/member-api.md`를 생성합니다. 문서에는 프로필 등록·조회와 오류별 HTTP 요청·응답 예시가 포함됩니다. `.\gradlew.bat build`에도 같은 검증이 포함됩니다.

## 개발 규칙

개발과 기여 방법은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고하세요.
