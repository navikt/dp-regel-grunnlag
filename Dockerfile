FROM ghcr.io/navikt/baseimages/temurin:11

ENV APPD_ENABLED=true \
    APPD_NAME=dp-regel-grunnlag

COPY build/libs/*-all.jar app.jar
