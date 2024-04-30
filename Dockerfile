FROM openjdk:20-ea-1-jdk
COPY /target/c350-loot.jar java-application.jar
ENV MYSQL_HOST=mysql-lootdb-comp350-loot.j.aivencloud.com
ENV MYSQL_HOST_PORT=22727
ENV MYSQL_DB_NAME=production
ENV MYSQL_USERNAME=avnadmin
EXPOSE 8080
CMD ["java", "-jar", "java-application.jar"]