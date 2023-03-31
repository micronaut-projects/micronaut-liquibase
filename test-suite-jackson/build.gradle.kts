plugins {
    id("io.micronaut.build.internal.liquibase.test-suite-module")
}

description = "Test suite for Liquibase + Jackson"

dependencies {
    testImplementation("io.micronaut:micronaut-jackson-databind")
}
