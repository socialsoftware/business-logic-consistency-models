# http://whitfin.io/speeding-up-maven-docker-builds/

FROM maven:3.8.4-openjdk-17 as base

COPY ./pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./src ./src

FROM base as test
CMD ["mvn", "-Ptest", "test"]

FROM base as run
ARG PROFILE=dev
RUN mvn -P$PROFILE -Dmaven.test.skip=true package

FROM azul/zulu-openjdk-alpine:17
COPY --from=run target/BusinessLogicConsistencyModels-0.0.1-SNAPSHOT.jar ./
CMD ["java","-Djava.security.egd=file:/dev/./urandom","-jar", "./BusinessLogicConsistencyModels-0.0.1-SNAPSHOT.jar"]

