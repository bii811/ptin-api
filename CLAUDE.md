# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project state

This is a freshly scaffolded Spring Boot project (generated via Spring Initializr) with no domain
code yet — only the application entry point (`PtinApplication`) and a default context-load test.
There is no established package structure, layering convention, or architecture to follow yet;
when adding the first real code, choose and apply a sensible structure rather than assuming one
already exists.

## Stack

- Java 21 (via Gradle toolchain), Gradle Kotlin DSL (`build.gradle.kts`), wrapper-only (no local Gradle install required)
- Spring Boot 4.1.0
- Dependencies: Spring Web MVC, Spring Data JPA, Spring Security, Spring Boot Actuator, PostgreSQL driver, Lombok, DevTools (dev-only)
- Base package: `com.example.ptin`

## Commands

Always use the Gradle wrapper (`./gradlew`), not a system-installed `gradle`.

```bash
./gradlew build              # full build (compiles + runs tests)
./gradlew bootRun            # run the application locally
./gradlew test               # run all tests
./gradlew test --tests "com.example.ptin.PtinApplicationTests"   # run a single test class
./gradlew test --tests "*.PtinApplicationTests.contextLoads"     # run a single test method
```

Note: `PtinApplicationTests` is a `@SpringBootTest` context-load test — it will try to connect to
a datasource (PostgreSQL) since `spring-boot-starter-data-jpa` and the PostgreSQL driver are on the
classpath. Ensure a reachable database (or an appropriate test datasource override) before relying
on this test to pass.

## Configuration

Application config lives in `src/main/resources/application.yaml` (currently just sets
`spring.application.name`). No datasource, security, or actuator settings have been configured yet.
