# 기여 가이드

개발 규칙은 [NIRVANA 글로벌 개발 컨벤션](https://github.com/Spharos8th-Nirvana/nirvana-docs/blob/main/nirvana-conventions/README.md)을 참고하세요.

- 최신 `develop`에서 작업 브랜치를 만들고 `develop`으로 PR을 엽니다.
- `main`과 `develop`에는 PR로 병합합니다. 병합 방식은 Merge Commit을 사용합니다.
- PR에는 관련 이슈, 변경 범위, 검증 결과, API·DB·이벤트 변경 여부를 기록합니다.
- 비밀번호, 토큰, 실제 환경변수 값, 내부 URL, 실사용 개인정보는 커밋하지 않습니다.

## 작업 브랜치 자동화

- `feature/`, `fix/`, `bugfix/`, `hotfix/`, `refactor/`, `chore/`, `docs/`, `test/` 브랜치를 처음 푸시하면 이슈가 생성됩니다. `feature/42-short-title`처럼 브랜치 이름에 열린 이슈 번호가 있으면 해당 이슈를 재사용합니다.
- 연결한 이슈와 GitHub에서 직접 연 이슈는 [NIRVANA Project](https://github.com/orgs/Spharos8th-Nirvana/projects/5)에 등록됩니다. 저장소 또는 조직의 Actions 시크릿 `NIRVANA_PROJECT_TOKEN`에 해당 프로젝트 쓰기 권한이 필요합니다.
- 작업 브랜치의 PR이 `develop` 또는 `main`에 병합되면 연결한 이슈를 완료로 닫습니다. 병합 없이 PR을 닫으면 이슈는 계속 열려 있습니다. GitHub의 브랜치 자동 삭제 설정은 병합한 원격 브랜치에 적용됩니다.
- 이슈 추적을 생략하는 저장소 자동화 작업은 `automation/` 브랜치를 사용할 수 있습니다.
