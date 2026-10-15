# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
# Wrapper + build scripts first: the Gradle distribution download is cached until these change.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY src src
# The cache mount keeps downloaded dependencies and the wrapper distribution between builds,
# so a source-only change doesn't re-download anything.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon --console=plain -x test bootJar \
    && java -Djarmode=tools -jar build/libs/$(ls build/libs | grep -v plain | head -1) \
        extract --layers --launcher --destination /extracted

FROM eclipse-temurin:21-jre
ARG PROFILE=default
ENV SPRING_PROFILES_ACTIVE=${PROFILE}
RUN groupadd --system app && useradd --system --gid app --no-create-home app
WORKDIR /app
# Least-changing layers first so code-only changes rebuild just the last COPY.
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
