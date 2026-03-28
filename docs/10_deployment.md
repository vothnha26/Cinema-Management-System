# 🚀 Deployment – Cinema Management System

## 1. Môi trường chạy

### Local Development

```bash
# 1. Clone project
git clone <repo-url>
cd cinema-management-system

# 2. Tạo database MySQL
mysql -u root -p
CREATE DATABASE cinema_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. Cấu hình application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/cinema_db
spring.datasource.username=root
spring.datasource.password=your_password

# 4. Chạy ứng dụng
mvn spring-boot:run

# 5. Truy cập
# Backend API: http://localhost:8080/api
# Frontend:    http://localhost:8080
```

---

## 2. Cấu trúc môi trường (Profiles)

```
application.properties          ← Cấu hình chung
application-dev.properties      ← Local development
application-prod.properties     ← Production (không commit lên git)
```

```properties
# application-dev.properties
spring.jpa.hibernate.ddl-auto=update    # Tự update schema
spring.jpa.show-sql=true                # Log SQL ra console
logging.level.com.example.cinema=DEBUG

# application-prod.properties
spring.jpa.hibernate.ddl-auto=validate  # Chỉ validate, không tự thay đổi
spring.jpa.show-sql=false
logging.level.root=WARN
```

```bash
# Chạy với profile cụ thể
mvn spring-boot:run -Dspring.profiles.active=dev
```

---

## 3. Build Production JAR

```bash
# Build
mvn clean package -DskipTests

# Chạy JAR
java -jar target/cinema-management-0.0.1-SNAPSHOT.jar \
     --spring.profiles.active=prod
```

---

## 4. Docker (Tùy chọn)

### Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/cinema-management-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: cinema_db
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/cinema_db
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
    depends_on:
      - mysql

volumes:
  mysql_data:
```

```bash
# Chạy toàn bộ stack
docker-compose up -d

# Kiểm tra log
docker-compose logs -f app
```

---

## 5. CI/CD (Đơn giản với GitHub Actions)

```yaml
# .github/workflows/build.yml
name: Build & Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: mvn clean test
      - run: mvn package -DskipTests
```

---

## 6. Checklist Deploy

- [ ] Tạo database `cinema_db` trên server
- [ ] Cấu hình `application-prod.properties` (không commit vào git)
- [ ] Set biến môi trường cho JWT secret
- [ ] `mvn clean package -DskipTests`
- [ ] Upload JAR lên server
- [ ] Chạy với `java -jar app.jar --spring.profiles.active=prod`
- [ ] Kiểm tra `GET /api/movies` trả data
- [ ] Kiểm tra `POST /api/auth/login` trả JWT
