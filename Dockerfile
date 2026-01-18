FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw

COPY pom.xml .
RUN ./mvnw dependency:go-offline || true

COPY src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN useradd -r -u 1001 spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]