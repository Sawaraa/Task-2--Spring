# Етап 1: Збірка (Maven + Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Завантажуємо залежності окремо для кешування
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Етап 2: Запуск (JRE 21)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Копіюємо jar файл (назва залежить від artifactId в pom.xml)
COPY --from=build /app/target/Task-2--Spring-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]