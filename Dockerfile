FROM amazoncorretto:21

LABEL version="1.0"
LABEL maintainer="prashant"
LABEL description="Course Service"

WORKDIR /app
ADD /target/course-service.jar course-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "course-service.jar"]
CMD ["--server.port=8085"]

# command to build the docker image
# docker build -t course-service .