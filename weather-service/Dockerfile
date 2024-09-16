FROM amazoncorretto:21-alpine
WORKDIR /app
COPY build/libs/weather-service-0.0.1-SNAPSHOT.jar /app/weather-service.jar
EXPOSE 9094
ENTRYPOINT ["java", "-jar", "/app/weather-service.jar"]