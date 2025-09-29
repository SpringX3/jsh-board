# =================
# 1. 빌드(Build) 단계 - 최적화 적용
# =================

FROM openjdk:21-jdk-slim AS builder

WORKDIR /app

# 1. 의존성 관련 파일만 먼저 복사합니다.
# (자주 변경되지 않는 파일들을 먼저 복사하여 캐시 활용 극대화)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 2. 의존성만 미리 다운로드하여 별도의 레이어를 생성합니다.
# (소스 코드가 변경되어도 이 레이어는 캐시된 상태로 유지됩니다.)
# 'dependencies' 태스크는 의존성만 해결하고 다운로드합니다.
RUN ./gradlew dependencies

# 3. 소스 코드를 복사합니다.
# (자주 변경되는 파일들은 의존성 레이어 생성 이후에 복사합니다.)
COPY src ./src

# 4. 애플리케이션을 빌드합니다.
# (의존성은 이미 다운로드 되어 있으므로 컴파일만 수행하여 빌드 속도가 매우 빨라집니다.)
RUN ./gradlew build -x test

# =================
# 2. 실행(Runtime) 단계
# =================

FROM openjdk:21-slim-bullseye

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java",  "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]