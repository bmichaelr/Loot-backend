FROM openjdk:20-ea-1
COPY /target/*.jar loot/server.jar
CMD ["java", "-jar", "loot/server.jar"]