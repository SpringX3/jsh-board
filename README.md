# Spring Boot 게시판 프로젝트

이 프로젝트는 Spring Boot를 사용하여 개발된 간단한 CRUD 게시판 애플리케이션입니다.

## ✨ 주요 기능

-   **게시글 생성 (Create)**: 새로운 게시글을 작성하고 저장합니다.
-   **게시글 조회 (Read)**: 특정 ID의 게시글 또는 전체 게시글 목록을 조회합니다.
-   **게시글 수정 (Update)**: 기존 게시글의 제목과 내용을 수정합니다.
-   **게시글 삭제 (Delete)**: 특정 ID의 게시글을 삭제합니다.

## 🛠️ 기술 스택

-   **Backend**: Java 21, Spring Boot 3.3.5
-   **Database**: MySQL
-   **Build Tool**: Gradle
-   **Dependencies**:
    -   Spring Web
    -   Spring Data JPA
    -   Spring Security
    -   Lombok
    -   Validation
    -   Springdoc (for API documentation)
    -   Docker Compose

## 🚀 실행 방법

### 1. Docker Compose 사용 (권장)

프로젝트 루트 디렉토리에서 아래 명령어를 실행하면 애플리케이션과 MySQL 데이터베이스가 함께 실행됩니다.

```bash
docker-compose up --build
```

### 2. Gradle 사용

로컬에 MySQL 데이터베이스가 실행 중이어야 합니다.

1.  **프로젝트 빌드**
    ```bash
    ./gradlew build
    ```

2.  **애플리케이션 실행**
    ```bash
    ./gradlew bootRun
    ```

## 📝 API 문서

애플리케이션 실행 후, 아래 URL에서 API 문서를 확인할 수 있습니다.

-   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 📂 프로젝트 구조

```
src
└── main
    └── java
        └── jsh
            └── board
                ├── BoardApplication.java   # 메인 애플리케이션
                ├── config                  # 설정 클래스
                ├── controller              # API 엔드포인트 처리
                ├── domain                  # JPA 엔티티
                ├── dto                     # 데이터 전송 객체
                ├── repository              # 데이터베이스 접근
                └── service                 # 비즈니스 로직
```

## 🏛️ SOLID 원칙 준수 설계

이 프로젝트는 Spring 프레임워크의 특성을 활용하여 SOLID 원칙을 따르도록 설계되었습니다. 핵심은 **계층형 아키텍처(Layered Architecture)**와 **의존성 주입(Dependency Injection)**을 통해 각 컴포넌트의 역할을 명확히 분리하는 것입니다.

### 1. S: 단일 책임 원칙 (Single Responsibility Principle)

각 클래스는 하나의 책임만 가집니다.

-   `PostController`: HTTP 요청을 받고, 유효성을 검사하며, `Service`를 호출하는 책임만 집니다.
-   `PostService`: 게시글과 관련된 비즈니스 로직을 처리하는 책임만 집니다.
-   `PostRepository`: 데이터베이스와의 통신(CRUD) 책임만 집니다.

### 2. O: 개방-폐쇄 원칙 (Open/Closed Principle)

기능 확장에 열려있고, 코드 수정에는 닫혀있어야 합니다. `Service`가 `Repository` 인터페이스에 의존하므로, 데이터베이스 기술을 바꾸거나 테스트 시 Mock 객체를 사용하기 위해 `Service` 코드를 수정할 필요가 없습니다.

### 3. L: 리스코프 치환 원칙 (Liskov Substitution Principle)

Spring Data JPA가 `JpaRepository` 인터페이스의 구현체를 자동으로 제공합니다. 이 구현체는 `JpaRepository`의 모든 규약을 완벽히 따르므로, 어떤 구현체가 주입되더라도 `Service`는 문제없이 동작합니다.

### 4. I: 인터페이스 분리 원칙 (Interface Segregation Principle)

`JpaRepository`는 `CrudRepository`, `PagingAndSortingRepository` 등 여러 작은 인터페이스의 조합으로 이루어져 있습니다. 이를 통해 클라이언트는 자신이 필요한 기능만 가진 인터페이스를 선택하여 사용할 수 있습니다.

### 5. D: 의존관계 역전 원칙 (Dependency Inversion Principle)

상위 계층이 하위 계층의 구체적인 구현에 의존하지 않고, **추상화(인터페이스)**에 의존합니다. 이것이 이 아키텍처의 핵심입니다.

-   **핵심 코드**: `PostService`는 `PostRepository`의 **구현체**가 아닌 **인터페이스**에 의존합니다. Spring의 DI 컨테이너가 런타임에 적절한 구현체를 주입해줍니다.

    ```java
    // PostService.java
    @Service
    @RequiredArgsConstructor
    public class PostService {

        private final PostRepository postRepository; // 구체 클래스가 아닌 인터페이스에 의존

        // ... 비즈니스 로직
    }
    ```

    ```java
    // PostRepository.java
    public interface PostRepository extends JpaRepository<Post, Long> {
        // Spring Data JPA가 자동으로 구현체를 생성
    }
    ```

이러한 설계 덕분에 각 컴포넌트의 독립성이 높아지고, 코드의 유연성과 테스트 용이성이 크게 향상됩니다.
