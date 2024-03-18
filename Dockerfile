FROM openjdk:20-ea-1-jdk

COPY /target/c350-loot.jar java-application.jar

EXPOSE 8080

CMD ["java", "-jar", "java-application.jar"]