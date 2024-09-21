FROM openjdk:22-slim
WORKDIR /app
COPY /app/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "-port=$PORT"]
