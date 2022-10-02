ARG BASE_IMAGE=amazoncorretto:17.0.4-al2
FROM ${BASE_IMAGE}

ARG APP_VERSION

VOLUME /tmp

EXPOSE 5080
EXPOSE 5081
EXPOSE 5025

RUN yum update --assumeyes --skip-broken && yum clean all

ADD build/libs/rabbitmq-queue-management-$APP_VERSION.jar /opt/rabbitmq-queue-management.jar
RUN ["touch", "/opt/rabbitmq-queue-management.jar"]
ENV JAVA_OPTS=""
ENTRYPOINT exec java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /opt/rabbitmq-queue-management.jar