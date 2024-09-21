FROM openjdk:22-slim
WORKDIR /app
COPY app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]