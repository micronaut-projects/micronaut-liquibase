plugins {
    id("groovy")
    id("java-library")
}

repositories {
    mavenCentral()
}

val micronautVersion: String by project
val groovyVersion: String by project

dependencies {
    testImplementation("org.codehaus.groovy:groovy-all:$groovyVersion")
    testImplementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation("io.micronaut:micronaut-http-server-netty")
    testImplementation("io.micronaut:micronaut-inject-groovy")
    testImplementation(project(":liquibase"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("io.micronaut.sql:micronaut-jdbc-hikari")
    testImplementation("io.micronaut.test:micronaut-test-spock")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("io.micronaut.serde:micronaut-serde-jackson")

    testRuntimeOnly("ch.qos.logback:logback-classic")
    testRuntimeOnly(libs.h2)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
