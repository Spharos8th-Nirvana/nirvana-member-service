# nirvana-member-service

## 서비스 개요

- 목적: 회원 도메인 기능을 담당한다.
- 담당자 / 백업 리뷰어: 미정 — 팀 확인 후 기입
- 소유 데이터와 운영 책임: 세부 데이터 소유 범위와 운영 책임은 팀 확인 후 기입
- API 문서: [Swagger UI](http://localhost:8082/swagger-ui/index.html)
- 기본 포트 / 헬스체크: `8082` (임시) / `GET /actuator/health`

초기 설정 검증용 서비스이며 비즈니스 기능은 아직 구현하지 않았습니다.
포트는 임시값이며 담당자·백업 리뷰어·세부 데이터 소유 범위는 미정입니다.
현재 기본 local 실행에는 JDK 21이 필요하며 외부 인프라 연결은 추가하지 않았습니다.
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
현재 기본 CI는 develop·main 대상 PR에서 bootJar를 실행합니다.
build에 포함된 테스트 실행과 CI의 실제 검사 범위는 구분해서 확인하세요.

## 개발 규칙

개발과 기여 방법은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고하세요.
