FROM openjdk:8-jre-slim
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} android.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/android.jar"]
