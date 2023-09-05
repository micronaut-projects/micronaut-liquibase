plugins {
    id("io.micronaut.build.internal.liquibase.test-suite-graal")
}

dependencies {
    runtimeOnly(mnSql.mariadb.java.client)
    implementation(mnTestResources.micronaut.test.resources.extensions.junit.platform)
}
