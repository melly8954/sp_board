## Spring-Board 게시판 토이 프로젝트 (BE)

<br><br>

## 프로젝트 실행 방법

**1️⃣ 환경 변수 설정**
프로젝트 루트에 `.env` 파일 생성 후, 필요한 환경 변수를 설정합니다.
```bash
# JWT 시크릿 키
JWT_SECRET_KEY=your_jwt_secret_key

# MySQL 설정
DB_NAME=your_database_name
DB_USER=your_database_user
DB_PASSWORD=your_database_password
MYSQL_ROOT_PASSWORD=your_mysql_root_password

# 로컬 개발용 포트
MYSQL_LOCAL_PORT=3306
REDIS_LOCAL_PORT=6379

# 도커 환경용 포트
MYSQL_HOST_PORT=3307
REDIS_HOST_PORT=6380
```

**2️⃣ Gradle 빌드**
```bash
# Windows(CMD) -> gradlew.bat build
./gradlew build
```

**3️⃣ DB 실행 (Docker Compose)**
```bash
docker-compose up -d
```


**4️⃣ Docker 이미지 빌드**
```bash
docker build -t sp_board:latest .
```

**5️⃣ 애플리케이션 실행**
```bash
docker run -p 8080:8080 --network sp_board_default --env-file .env -e "SPRING_PROFILES_ACTIVE=docker" --name spring-board-app equip-rental:latest
```
<hr>
