FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml /workspace/pom.xml
COPY src /workspace/src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/letter-gen-backend.jar /app/letter-gen-backend.jar
RUN mkdir -p /app/data/generated-letters
EXPOSE 9090
ENV SERVER_PORT=9090
ENV APP_STORAGE_BASE_DIRECTORY=/app/data/generated-letters
ENV APP_STORAGE_METADATA_FILE_NAME=letters-index.csv
ENTRYPOINT ["java", "-jar", "/app/letter-gen-backend.jar"]