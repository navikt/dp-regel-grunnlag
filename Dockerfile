FROM navikt/java:11-appdynamics

ENV APPD_ENABLED=true \
    APPD_NAME=dp-regel-grunnlag

COPY build/libs/*.jar app.jar
