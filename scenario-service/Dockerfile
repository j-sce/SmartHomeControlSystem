FROM amazoncorretto:21-alpine
WORKDIR /app
COPY build/libs/scenario-service-0.0.1-SNAPSHOT.jar /app/scenario-service.jar
EXPOSE 9093
ENTRYPOINT ["java", "-jar", "/app/scenario-service.jar"]