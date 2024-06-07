FROM openjdk:17

WORKDIR /app

COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn
COPY src ./src

RUN chmod +x mvnw

RUN ./mvnw clean package

ENTRYPOINT ["java", "-jar", "target/warnme-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080