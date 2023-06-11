package io.micronaut.liquibase

import groovy.sql.Sql
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.inject.qualifiers.Qualifiers
import spock.lang.AutoCleanup
import spock.lang.PendingFeature
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.sql.DataSource
import java.sql.SQLException

class LiquibaseMigratorSpec extends Specification {

    @Shared
    Map<String, Object> config = [
        'jpa.default.packages-to-scan'                 : ['example.micronaut'],
        'jpa.default.properties.hibernate.hbm2ddl.auto': 'none',
        'jpa.default.properties.hibernate.show_sql'    : true,

        'datasources.default.url'                      : 'jdbc:h2:mem:liquibaseMigratorDb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=TRUE',
        'datasources.default.username'                 : 'sa',
        'datasources.default.password'                 : '',
        'datasources.default.driverClassName'          : 'org.h2.Driver',

        'liquibase.enabled'                            : true,
        'liquibase.datasources.default.enabled'        : false,
        'liquibase.datasources.default.change-log'     : 'classpath:db/liquibase-changelog.xml'
    ]

    @Shared
    @AutoCleanup
    ApplicationContext applicationContext = ApplicationContext.run(config as Map<String, Object>, Environment.TEST)

    @PendingFeature
    void 'when migrations are disabled it is possible to run the using the LiquibaseMigrator'() {
        when:
        LiquibaseMigrator liquibaseMigrator = applicationContext.getBean(LiquibaseMigrator)
        DataSource dataSource = applicationContext.getBean(DataSource, Qualifiers.byName("default"))
        LiquibaseConfigurationProperties liquibaseConfigurationProperties = applicationContext.getBean(LiquibaseConfigurationProperties)
        PollingConditions conditions = new PollingConditions(timeout: 5)
        Sql sql = Sql.newInstance(config.get('datasources.default.url'),
                config.get('datasources.default.username'),
                config.get('datasources.default.password'),
                config.get('datasources.default.driverClassName'))
        String countBooks = 'select count(*) from books'

        then:
        noExceptionThrown()

        when:
        sql.rows(countBooks)

        then: 'the migration was not already run'
        thrown(SQLException)

        when:
        liquibaseMigrator.run(liquibaseConfigurationProperties, dataSource)

        then:
        noExceptionThrown()
        conditions.eventually {
            sql.rows(countBooks).get(0)[0] == 2
        }

        cleanup:
        sql.close()
    }

}
