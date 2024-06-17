FROM amazoncorretto:17-alpine

ARG JAR_FILE=target/paymybuddy.jar

WORKDIR /app

COPY ${JAR_FILE} paymybuddy.jar

ENV SPRING_DATASOURCE_USERNAME=user_bd

ENV SPRING_DATASOURCE_PASSWORD=password_bd

# SPRING_DATASOURCE_URL=jdbc:mysql://<ip_docker0>:3306/db_paymybuddy
ENV SPRING_DATASOURCE_URL=url_bd 

CMD ["java", "-jar" , "paymybuddy.jar"]
