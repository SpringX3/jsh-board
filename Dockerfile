# =================
# 1. 빌드(Build) 단계
# =================

FROM openjdk:21-jdk-slim AS builder

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# 프로젝트의 모든 파일을 컨테이너의 /app 디렉토리로 복사합니다.
COPY . .

# Gradle Wrapper를 사용하여 프로젝트를 빌드합니다. 테스트는 생략합니다.
RUN ./gradlew build -x test

# =================
# 2. 실행(Runtime) 단계
# =================

FROM openjdk:21-slim-bullseye

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# 빌드 단계('builder')에서 생성된 JAR 파일을 실행 단계의 컨테이너로 복사합니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션이 8080 포트를 사용함을 명시합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 실행할 명령어를 정의합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]