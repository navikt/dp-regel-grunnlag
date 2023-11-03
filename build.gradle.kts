plugins {
    id("common")
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
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

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Multi-Release"] = "true" // https://github.com/johnrengelman/shadow/issues/449
    }
}

val moshiVersion = "1.15.0"
val log4j2Versjon = "2.21.1"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.navikt:dagpenger-events:2023081713361692272216.01ab7c590338")
    implementation("com.github.navikt:dagpenger-streams:20230831.f3d785")
    implementation("com.github.navikt:dp-grunnbelop:2023.05.24-15.26.f42064d9fdc8")

    implementation("com.squareup.moshi:moshi:$moshiVersion")
    implementation("com.squareup.moshi:moshi-adapters:$moshiVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")

    implementation("de.huxhorn.sulky:de.huxhorn.sulky.ulid:8.3.0")

    implementation("io.prometheus:simpleclient_common:0.16.0")
    implementation("io.prometheus:simpleclient_hotspot:0.16.0")
    implementation("no.nav:nare-prometheus:0b41ab4")

    implementation("org.apache.kafka:kafka-streams:3.3.1")
    implementation("org.apache.kafka:kafka-clients:3.3.1")

    implementation(libs.konfig)

    implementation("io.getunleash:unleash-client-java:8.4.0")

    implementation(libs.kotlin.logging)
    implementation("org.apache.logging.log4j:log4j-api:$log4j2Versjon")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4j2Versjon")
    implementation("org.apache.logging.log4j:log4j-layout-template-json:$log4j2Versjon")
    implementation("org.apache.logging.log4j:log4j-core:$log4j2Versjon")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:${libs.versions.junit.get()}")
    testImplementation(libs.kotest.assertions.core)
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${libs.versions.kotest.get()}")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:3.3.1")
    testImplementation(libs.mockk)
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
}
