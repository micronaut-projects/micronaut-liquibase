plugins {
    id("io.micronaut.build.internal.liquibase.test-suite-module")
}

description = "Test suite for Liquibase + Jackson"

dependencies {
    testImplementation(mnSerde.micronaut.serde.api)
    testImplementation(mnSerde.micronaut.serde.jackson)
}
