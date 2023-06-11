plugins {
    id("io.micronaut.application")
    id("io.micronaut.test-resources")
    id("io.micronaut.build.internal.liquibase-native-tests")
    id("io.micronaut.build.internal.liquibase.test-suite-graal")
}

dependencies {
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")
}

application {
    mainClass.set("micronaut.example.Application")
}

micronaut {
    version(libs.versions.micronaut.platform.get())
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(false)
        annotations("micronaut.example.*")
    }
}

