plugins {
    id("common")
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

application {
    applicationName = "dp-regel-grunnlag"
    mainClass.set("no.nav.dagpenger.regel.grunnlag.ApplicationKt")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.rapids.and.rivers)

    implementation("com.github.navikt:dp-inntekt-kontrakter:1_20231220.55a8a9")
    implementation("com.github.navikt:dagpenger-events:20231220.3050bf")
    implementation("com.github.navikt:dp-grunnbelop:2024.05.13-12.07.fec45f516acf")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    implementation("io.prometheus:simpleclient_common:0.16.0")
    implementation("io.prometheus:simpleclient_hotspot:0.16.0")
    implementation("no.nav:nare-prometheus:0b41ab4")

    implementation(libs.konfig)

    implementation("io.getunleash:unleash-client-java:9.2.2")

    implementation(libs.kotlin.logging)

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation(libs.kotest.assertions.core)
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${libs.versions.kotest.get()}")
    testImplementation(libs.mockk)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
}
