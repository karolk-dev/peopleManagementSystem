FROM openjdk:21-jdk

WORKDIR /app

COPY target/people-management-system-0.0.1-SNAPSHOT.jar /app/people-management-system.jar

ENTRYPOINT ["java", "-jar", "/app/people-management-system.jar"]