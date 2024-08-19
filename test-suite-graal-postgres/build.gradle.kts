plugins {
    id("io.micronaut.build.internal.liquibase.test-suite-graal")
}
dependencies {
    testImplementation(platform(mnTestResources.boms.testcontainers))
    testImplementation(libs.testcontainers.junit.jupiter)
}
dependencies {
    runtimeOnly(mnSql.postgresql)
    implementation(mnTestResources.micronaut.test.resources.extensions.junit.platform) {
        exclude(group = "org.jetbrains.kotlin")
    }
}
