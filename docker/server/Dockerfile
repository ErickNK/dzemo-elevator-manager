FROM openjdk:11-jre-slim
LABEL maintainer="erick.kirabui@gmail.com"
EXPOSE 8081
WORKDIR /opt/app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} elevator-manager.jar

ENV TZ=Africa/Nairobi
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENTRYPOINT ["java","-jar","elevator-manager.jar"]
