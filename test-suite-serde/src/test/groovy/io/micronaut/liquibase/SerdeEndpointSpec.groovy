package io.micronaut.liquibase

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification

class SerdeEndpointSpec extends Specification {

    void "can read endpoint using Serde"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(
                EmbeddedServer,
                ['jpa.default.packages-to-scan'                 : 'example.micronaut',
                 'jpa.default.properties.hibernate.hbm2ddl.auto': 'none',
                 'jpa.default.properties.hibernate.show_sql'    : true,
                 'liquibase.datasources.default.change-log'     : 'classpath:db/liquibase-changelog.xml',
                 'endpoints.liquibase.sensitive'                : false,
                 'datasources.default.url'                      : 'jdbc:h2:mem:liquibaseEndpointDb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE',
                 'datasources.default.username'                 : 'sa',
                 'datasources.default.password'                 : '',
                 'datasources.default.driver-class-name'        : 'org.h2.Driver'] as Map,
                Environment.TEST
        )
        URL server = embeddedServer.getURL()
        HttpClient client = embeddedServer.applicationContext.createBean(HttpClient, server)

        when:
        def response = client.toBlocking().retrieve(HttpRequest.GET("/liquibase"), Argument.listOf(Map))

        then:
        response.name == ["default"]
        response.changeSets.author == [['sdelamo', 'sdelamo']]
        response.changeSets.description == [['createTable tableName=books', 'insert tableName=books; insert tableName=books']]
    }
}
