plugins {
    id("common")
    application
    alias(libs.plugins.shadow.jar)
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

application {
    applicationName = "dp-regel-grunnlag"
    mainClass.set("no.nav.dagpenger.regel.grunnlag.ApplicationKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.rapids.and.rivers)

    implementation("com.github.navikt:dp-inntekt-kontrakter:2_20251211.17f9d7")
    implementation("com.github.navikt:dp-grunnbelop:20250526.166.5b778c")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    implementation("io.prometheus:simpleclient_common:0.16.0")
    implementation("io.prometheus:simpleclient_hotspot:0.16.0")
    implementation("no.nav:nare-prometheus:0b41ab4")

    implementation(libs.konfig)

    implementation("io.getunleash:unleash-client-java:12.0.1")

    implementation(libs.kotlin.logging)

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation(libs.kotest.assertions.core)
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${libs.versions.kotest.get()}")
    testImplementation(libs.mockk)
    testImplementation(libs.rapids.and.rivers.test)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
}
