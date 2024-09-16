FROM amazoncorretto:21-alpine
WORKDIR /app
COPY build/libs/device-service-0.0.1-SNAPSHOT.jar /app/device-service.jar
EXPOSE 9092
ENTRYPOINT ["java", "-jar", "/app/device-service.jar"]