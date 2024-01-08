plugins {
    id("java-library")
    id("io.micronaut.build.internal.liquibase-base")
}

dependencies {
    implementation(mn.micronaut.http.server)
    implementation(mn.micronaut.context)
    implementation(mn.micronaut.inject)
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnData.micronaut.data.processor)
    annotationProcessor(mnValidation.micronaut.validation.processor)
    implementation(mnValidation.micronaut.validation)
    implementation(mnData.micronaut.data.jdbc)
    implementation(mnSql.micronaut.jdbc.hikari)
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    // JUL to Slf4j bridge so Liquibase log works with Logback
    implementation("org.slf4j:jul-to-slf4j:2.0.11")
}
