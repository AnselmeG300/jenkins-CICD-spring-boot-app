FROM amazoncorretto:17-alpine

ARG JAR_FILE=target/paymybuddy.jar

WORKDIR /app

COPY ${JAR_FILE} paymybuddy.jar

CMD ["java", "-jar" , "paymybuddy.jar"]
