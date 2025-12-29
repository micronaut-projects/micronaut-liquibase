plugins {
    id("io.micronaut.build.internal.liquibase.test-suite-graal")
}
dependencies {
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
    runtimeOnly(mnSql.mariadb.java.client)
    testImplementation(libs.testcontainers.mariadb)
    testImplementation(mnTest.junit.platform.launcher)
}
