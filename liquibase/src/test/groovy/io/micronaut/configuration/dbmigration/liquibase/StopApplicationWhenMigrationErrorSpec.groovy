package io.micronaut.configuration.dbmigration.liquibase

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.runtime.exceptions.ApplicationStartupException
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.Shared
import spock.lang.Specification

class StopApplicationWhenMigrationErrorSpec extends Specification {

    @Shared
    Map<String, Object> config = [
        'datasources.default.url'                      : 'jdbc:h2:mem:stopApplicationDb',
        'datasources.default.username'                 : 'sa',
        'datasources.default.password'                 : '',
        'datasources.default.driverClassName'          : 'org.h2.Driver',

        'jpa.default.packages-to-scan'                 : ['example.micronaut'],
        'jpa.default.properties.hibernate.hbm2ddl.auto': 'none',
        'jpa.default.properties.hibernate.show_sql'    : true,

        'liquibase.datasources.default.async'          : false,
        'liquibase.datasources.default.change-log'     : 'classpath:db/liquibase-wrong-changelog.xml',
    ]

    void "test application context stops if there is an error with the migrations"() {
        when:
        ApplicationContext.run(EmbeddedServer, config as Map<String, Object>, Environment.TEST)

        then:
        def e = thrown(BeanInstantiationException)
        e.cause instanceof ApplicationStartupException
        e.cause.message == 'Migration failed! Liquibase encountered an exception.'
    }
}
