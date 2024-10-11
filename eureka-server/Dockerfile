FROM amazoncorretto:21-alpine
WORKDIR /app
COPY build/libs/eureka-server-0.0.1-SNAPSHOT.jar /app/eureka-server.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app/eureka-server.jar"]