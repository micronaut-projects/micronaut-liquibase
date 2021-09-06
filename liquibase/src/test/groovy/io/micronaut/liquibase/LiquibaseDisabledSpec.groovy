package io.micronaut.liquibase

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.context.exceptions.NoSuchBeanException
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource

class LiquibaseDisabledSpec extends Specification {

    @Shared
    Map<String, Object> config = [
        'jpa.default.packages-to-scan'                 : ['example.micronaut'],
        'jpa.default.properties.hibernate.hbm2ddl.auto': 'none',
        'jpa.default.properties.hibernate.show_sql'    : true,

        'datasources.default.url'                      : 'jdbc:h2:mem:liquibaseDisabledSpec;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE',
        'datasources.default.username'                 : 'sa',
        'datasources.default.password'                 : '',
        'datasources.default.driverClassName'          : 'org.h2.Driver',

        'liquibase.enabled'                            : false,
        'liquibase.datasources.default.change-log'     : 'classpath:db/liquibase-changelog.xml'
    ]

    @Shared
    @AutoCleanup
    ApplicationContext applicationContext = ApplicationContext.run(config as Map<String, Object>, Environment.TEST)

    void "if liquibase.enabled=false changelog are not run"() {

        when:
        applicationContext.getBean(DataSource)

        then:
        noExceptionThrown()

        when:
        applicationContext.getBean(LiquibaseConfigurationProperties)

        then:
        thrown(NoSuchBeanException)
    }
}
