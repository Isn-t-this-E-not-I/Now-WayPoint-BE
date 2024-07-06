# Now-WayPoint

## 개요

Now-WayPoint(나와)의 프로젝트 개요를 설명합니다.

## 프로젝트 구조

벡엔드 프로젝트 하위는 각 API서버 프로젝트(컨테이너 기반으로 구성됨)로 나눠져있습니다.
API서버는 아래 아키텍처 구조을 따르고 있으며 각 프로젝트는 다음과 같습니다.

![스크린샷 2024-06-28 오후 11 43 28](https://github.com/immyeong/immyeong.github.io/assets/62759873/58dceaf5-bdf1-4e0a-8887-21ed2eeab28e)

- now-waypoint-core : 인증, 사용자 관리, 게시글관리, 채팅, 좋아요 관리, 해시태그 관리 등 핵심 기능을 제공하는 코어 서버
- now-waypoint-mysql : MySQL 데이터베이스 서버

## 사전 준비

프로젝트를 개발하기 전 사전 준비 작업입니다.

- 본인 PC에 Docker 설치(https://www.docker.com/products/docker-desktop/)
- 개발IDE에 Docker 플러그인 설치(예: IntelliJ IDE의 Docker 플러그인)
- Docker Desktop 실행

## 빌드 및 테스트

크게 Local 빌드와 Docker 빌드로 나눠집니다.

### Local 빌드

평소처럼 본인 PC에서 개발하듯이 하면 됩니다.

### Docker 빌드

1. Docker Desktop 실행
2. 각 프로젝트의 Dockerfile을 이용하여 Docker 이미지를 빌드합니다.
   ```bash
   docker buildx build -t now-waypoint-core .
   docker buildx build -t now-waypoint-mysql .
   ```
3. 빌드된 이미지를 Docker 컨테이너로 실행합니다.
   ```bash
   docker run -d -p 8080:8080 now-waypoint-core
   docker run -d -p 3306:3306 now-waypoint-mysql
   ```
4. 각 컨테이너의 포트번호를 확인하고, API 서버에 접속합니다.(localhost:포트번호)

   - now-waypoint-core : 8080
   - now-waypoint-mysql : 3306

5. 테스트 확인 이후 컨테이너를 삭제합니다.
   ```bash
   docker rm -f $(docker ps -a -q)
   ```

## CI/CD 관점에서 완료사항

- 서버에 대한 샘플 dockerfile(core서버 기준) 작성완료(각 프로젝트의 Dockerfile)
- 각 컨테이너 포트번호 설정 및 통신여부 확인
- docker-compose를 통한 여러 컨테이너 동시 실행 확인
- github actions를 활용하기 위한 CI 설정값 구성(.github/workflows/ 하위 yml 파일들)

## CI/CD 관점에서 해야할 사항
- aws deploy, ec2 or s3 bucket을 사용하여 CD 설정값 구성

## 백엔드팀에서 앞으로 해야할 일
- 요구사항에 따른 기능 구현
- 서버에 대한 테스트 코드 작성
- CD 파이프라인 구성(aws)
