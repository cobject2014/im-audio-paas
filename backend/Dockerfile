FROM maven:3.9-eclipse-temurin-17 as builder
WORKDIR /app
COPY pom.xml .
# Only download dependencies if pom.xml changed
# Using verify --fail-never allows downloading dependencies even if compilation fails (which implies src not there yet)
# But standard go-offline is better
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
