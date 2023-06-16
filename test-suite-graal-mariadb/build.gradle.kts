plugins {
    id("io.micronaut.build.internal.liquibase.test-suite-graal")
}

dependencies {
    runtimeOnly(mnSql.mariadb.java.client)
}
