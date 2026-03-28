# ⚙️ Tech Stack – Cinema Management System

## 1. Bảng tổng hợp công nghệ

| Tầng | Công nghệ | Phiên bản | Mục đích |
|------|-----------|-----------|---------|
| **Backend** | Java | 17 / 21 | Ngôn ngữ chính |
| | Spring Boot | 3.x | Framework web |
| | Spring Data JPA | (cùng Boot) | ORM layer |
| | Spring Security | (cùng Boot) | Auth & phân quyền |
| | Hibernate | (cùng JPA) | ORM implementation |
| | Lombok | latest | Giảm boilerplate |
| | ModelMapper | 3.x | Entity ↔ DTO mapping |
| | Validation | (cùng Boot) | @Valid annotations |
| | JJWT | 0.12.x | JWT generate / validate |
| | Cloudinary | 1.36.x | Image Storage & Optimization |
| **Frontend** | HTML5 / CSS3 | - | Giao diện |

| | JavaScript (ES6+) | - | Logic frontend |
| | Bootstrap 5 | 5.3 | UI components, responsive |
| | Chart.js | 4.x | Biểu đồ thống kê doanh thu |
| | Fetch API | native | Gọi REST API |
| **Database** | MySQL | 8.0 | Lưu trữ chính |
| **Build** | Maven | 3.9+ | Dependency management |
| **IDE** | IntelliJ IDEA | 2023+ | Phát triển |

---

## 2. pom.xml – Dependencies chính

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- ModelMapper -->
    <dependency>
        <groupId>org.modelmapper</groupId>
        <artifactId>modelmapper</artifactId>
        <version>3.2.0</version>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 3. application.properties

```properties
# Server
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/cinema_db?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT
jwt.secret=cinema-management-super-secret-key-2024-at-least-256-bit
jwt.expiration=86400000

# File upload (poster)
spring.servlet.multipart.max-file-size=10MB
```

---

## 4. Tại sao chọn tech stack này?

| Quyết định | Lý do |
|-----------|-------|
| Spring Boot (không Node.js) | Yêu cầu Java, ecosystem mạnh, production-ready |
| MySQL (không MongoDB) | Dữ liệu quan hệ rõ ràng, cần JOIN phức tạp |
| JWT (không Session) | Stateless, phù hợp SPA và mobile sau này |
| Bootstrap 5 (không React) | Nhóm 3 người, không cần học thêm frontend framework |
| Maven (không Gradle) | Phổ biến trong môi trường học thuật |
| JPA (không MyBatis) | Giảm boilerplate SQL, phù hợp với SOLID |
