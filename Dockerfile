# 1. Build stage
FROM gradle:8.10-jdk17 AS build
WORKDIR /app

# 루트의 gradle 설정 파일들을 먼저 복사 (캐시 활용)
COPY educoon/settings.gradle educoon/build.gradle ./
# 의존성 먼저 다운로드
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

# 전체 소스 복사 및 빌드
COPY educoon/src ./src
RUN gradle bootJar -x test

# 2. Run stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드 스테이지에서 생성된 jar 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 실행 환경 설정
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
