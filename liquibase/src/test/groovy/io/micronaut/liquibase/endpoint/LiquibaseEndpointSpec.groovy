package io.micronaut.liquibase.endpoint

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.liquibase.endpoint.LiquibaseEndpoint
import io.micronaut.liquibase.endpoint.LiquibaseReport
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Specification

class LiquibaseEndpointSpec extends Specification {

    void "test liquibase endpoint bean is available"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(Environment.TEST)

        expect:
        applicationContext.containsBean(LiquibaseEndpoint)

        cleanup:
        applicationContext.close()
    }

    void "test liquibase the endpoint bean can be disabled"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
            ['endpoints.liquibase.enabled': false] as Map,
            Environment.TEST
        )

        expect:
        !applicationContext.containsBean(LiquibaseEndpoint)

        cleanup:
        applicationContext.close()
    }

    void "test the liquibase endpoint bean is not available with all endpoints disabled"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
            ['endpoints.all.enabled': false] as Map,
            Environment.TEST
        )

        expect:
        !applicationContext.containsBean(LiquibaseEndpoint)

        cleanup:
        applicationContext.close()
    }

    void "test the liquibase endpoint bean is available with all disabled but having it enabled"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run(
            ['endpoints.all.enabled'      : false,
             'endpoints.liquibase.enabled': true] as Map,
            Environment.TEST)

        expect:
        applicationContext.containsBean(LiquibaseEndpoint)

        cleanup:
        applicationContext.close()
    }

    void 'test liquibase endpoint'() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(
            EmbeddedServer,
            ['jpa.default.packages-to-scan'                 : 'example.micronaut',
             'jpa.default.properties.hibernate.hbm2ddl.auto': 'none',
             'jpa.default.properties.hibernate.show_sql'    : true,
             'liquibase.datasources.default.change-log'     : 'classpath:db/liquibase-changelog.xml',
             'endpoints.liquibase.sensitive'                : false,
             'datasources.default.url'                      : 'jdbc:h2:mem:liquibaseDisabledDb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
             'datasources.default.username'                 : 'sa',
             'datasources.default.password'                 : '',
             'datasources.default.driver-class-name'        : 'org.h2.Driver'] as Map,
            Environment.TEST
        )
        URL server = embeddedServer.getURL()
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, server)

        when:
        HttpResponse<List> response = rxClient.toBlocking()
            .exchange(HttpRequest.GET("/liquibase"), Argument.of(List, Map))

        then:
        response.status() == HttpStatus.OK
        List<LiquibaseReport> result = response.body()
        result.size() == 1
        result[0].name == 'default'
        result[0].changeSets.size() == 2
        result[0].changeSets[0].changeLog == 'db/changelog/01-create-books-schema.xml'
        result[0].changeSets[1].changeLog == 'db/changelog/02-insert-data-books.xml'

        cleanup:
        rxClient.close()
        embeddedServer.stop()
        embeddedServer.close()
    }

    void 'test liquibase endpoint with multiple datasources'() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(
            EmbeddedServer,
            ['jpa.default.packages-to-scan'                 : 'example.micronaut',
             'jpa.default.properties.hibernate.hbm2ddl.auto': 'none',
             'jpa.default.properties.hibernate.show_sql'    : true,
             'liquibase.datasources.default.change-log'     : 'classpath:db/liquibase-changelog.xml',
             'liquibase.datasources.other.change-log'       : 'classpath:db/liquibase-other-changelog.xml',
             'endpoints.liquibase.sensitive'                : false,
             'datasources.default.url'                      : 'jdbc:h2:mem:liquibaseDb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
             'datasources.default.username'                 : 'sa',
             'datasources.default.password'                 : '',
             'datasources.default.driver-class-name'        : 'org.h2.Driver',
             'datasources.other.url'                        : 'jdbc:h2:mem:liquibase2Db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
             'datasources.other.username'                   : 'sa',
             'datasources.other.password'                   : '',
             'datasources.other.driver-class-name'          : 'org.h2.Driver'] as Map,
            Environment.TEST
        )
        URL server = embeddedServer.getURL()
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, server)

        when:
        HttpResponse<List> response = rxClient.toBlocking()
            .exchange(HttpRequest.GET("/liquibase"), Argument.of(List, Map))

        then:
        response.status() == HttpStatus.OK
        List<LiquibaseReport> result = response.body()
        result.sort { it.name }
        result[0].name == 'default'
        result[0].changeSets.size() == 2
        result[0].changeSets[0].changeLog == 'db/changelog/01-create-books-schema.xml'
        result[0].changeSets[1].changeLog == 'db/changelog/02-insert-data-books.xml'
        result[1].name == 'other'
        result[1].changeSets.size() == 1
        result[1].changeSets[0].changeLog == 'db/changelog/01-create-books-schema.xml'

        cleanup:
        rxClient.close()
        embeddedServer.stop()
        embeddedServer.close()
    }

    void 'test liquibase endpoint without migrations'() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(
            EmbeddedServer,
            ['jpa.default.packages-to-scan'                 : 'example.micronaut',
             'jpa.default.properties.hibernate.hbm2ddl.auto': 'none',
             'jpa.default.properties.hibernate.show_sql'    : true,
             'endpoints.liquibase.sensitive'                : false,
             'datasources.default.url'                      : 'jdbc:h2:mem:liquibaseDisabledDb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
             'datasources.default.username'                 : 'sa',
             'datasources.default.password'                 : '',
             'datasources.default.driver-class-name'        : 'org.h2.Driver'] as Map,
            Environment.TEST
        )
        URL server = embeddedServer.getURL()
        RxHttpClient rxClient = embeddedServer.applicationContext.createBean(RxHttpClient, server)

        when:
        HttpResponse<List> response = rxClient.toBlocking()
            .exchange(HttpRequest.GET("/liquibase"), Argument.of(List, LiquibaseReport))

        then:
        response.status() == HttpStatus.OK
        List<LiquibaseReport> result = response.body()
        result.size() == 0

        cleanup:
        rxClient.close()
        embeddedServer.stop()
        embeddedServer.close()
    }
}
