FROM amazoncorretto:17-alpine

ARG JAR_FILE=target/paymybuddy.jar

WORKDIR /app

COPY ${JAR_FILE} paymybuddy.jar

ENV SPRING_DATASOURCE_USERNAME=root

ENV SPRING_DATASOURCE_PASSWORD=password

ENV SPRING_DATASOURCE_URL=jdbc:mysql://172.17.0.1:3306/db_paymybuddy

CMD ["java", "-jar" , "paymybuddy.jar"]
