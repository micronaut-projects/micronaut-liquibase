plugins {
    id("io.micronaut.build.internal.liquibase.test-suite-graal")
}
dependencies {
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    runtimeOnly(mnSql.postgresql)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(mnTest.junit.platform.launcher)
}
