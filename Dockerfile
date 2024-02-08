FROM vegardit/graalvm-maven:21.0.2 AS stage1

MAINTAINER Artem Konovalov <izebit@gmail.com>
LABEL maintainer="Artem Konovalov <izebit@gmail.com>"
LABEL description="This image with Phone Booking app"
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

WORKDIR /opt/app
COPY . .
RUN mvn package
FROM openjdk:23-oracle
WORKDIR /opt/app
COPY --from=stage1 /opt/app/target/*.jar /opt/app/app.jar

EXPOSE 8080
ENTRYPOINT java -jar /opt/app/app.jar