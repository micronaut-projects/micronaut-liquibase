package io.micronaut.liquibase

import groovy.sql.Sql
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.AutoCleanup
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.sql.DataSource

class LiquibaseAsyncSpec extends Specification {

    @Shared
    Map<String, Object> config = [
        'datasources.default.url'                      : 'jdbc:h2:mem:liquibaseAsyncSpec;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
        'datasources.default.username'                 : 'sa',
        'datasources.default.password'                 : '',
        'datasources.default.driverClassName'          : 'org.h2.Driver',

        'jpa.default.packages-to-scan'                 : ['example.micronaut'],
        'jpa.default.properties.hibernate.hbm2ddl.auto': 'none',
        'jpa.default.properties.hibernate.show_sql'    : true,

        'liquibase.datasources.default.async'          : true,
        'liquibase.datasources.default.dropFirst'      : true,
        'liquibase.datasources.default.change-log'     : 'classpath:db/liquibase-changelog.xml',
    ]

    @Shared
    @AutoCleanup
    ApplicationContext applicationContext = ApplicationContext.run(config as Map<String, Object>, Environment.TEST)

    @Ignore("https://github.com/micronaut-projects/micronaut-liquibase/issues/155")
    void "test liquibase changelog can be run asynchronously"() {
        when:
        applicationContext.getBean(DataSource)

        then:
        noExceptionThrown()

        when:
        LiquibaseConfigurationProperties config = applicationContext.getBean(LiquibaseConfigurationProperties)

        then:
        noExceptionThrown()
        config.getChangeLog() == 'classpath:db/liquibase-changelog.xml'

        when:
        PollingConditions conditions = new PollingConditions(timeout: 5)

        Map db = [url:'jdbc:h2:mem:liquibaseDisabledDb', user:'sa', password:'', driver:'org.h2.Driver']
        Sql sql = Sql.newInstance(db.url, db.user, db.password, db.driver)

        then:
        conditions.eventually {
            sql.rows('select count(*) from books').get(0)[0] == 2
        }
    }
}
