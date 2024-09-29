FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/demo-1.0-SNAPSHOT.jar .

EXPOSE 8000

ENTRYPOINT ["java", "-jar", "/app/demo-1.0-SNAPSHOT.jar"]
