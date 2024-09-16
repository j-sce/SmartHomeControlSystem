FROM amazoncorretto:21-alpine
WORKDIR /app
COPY build/libs/user-service-0.0.1-SNAPSHOT.jar /app/user-service.jar
EXPOSE 9091
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]