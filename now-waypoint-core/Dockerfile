# 베이스 이미지 설정
FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드 시점에 환경 변수 설정
ARG AWS_ACCESS_KEY_ID
ARG AWS_SECRET_ACCESS_KEY

# 환경 변수를 Docker 컨테이너 내부에서 사용할 수 있도록 설정
ENV AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
ENV AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}

# 빌드된 JAR 파일을 로컬에서 직접 가져오도록 설정
COPY ./build/libs/*-SNAPSHOT.jar /app/now-waypoint-core.jar

# 8080 포트를 외부에 노출
EXPOSE 8080


# 컨테이너 실행 시 자바 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "now-waypoint-core.jar"]