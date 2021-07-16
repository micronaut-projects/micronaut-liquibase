package io.micronaut.liquibase

import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.jdbc.DataSourceResolver
import jakarta.inject.Singleton

import javax.sql.DataSource

class LiquibaseMigrationRunnerSpec extends ApplicationContextSpecification {

    @Override
    String getSpecName() {
        'LiquibaseMigrationRunnerSpec'
    }
    @Override
    Map<String, Object> getConfiguration() {
        super.configuration +
                getDataSourceConfiguration('liquibaseDisabledDb') +
                getJpaConfiguration(['example.micronaut'])
    }
    void "LiquibaseMigrationRunner::onCreated returns wrapped Datasource"() {
        when:
        DataSource dataSource = applicationContext.getBean(DataSource)

        then:
        !(dataSource instanceof ReturnedByDataSourceResolver)
    }

    @Primary
    @Requires(property = 'spec.name', value = 'LiquibaseMigrationRunnerSpec')
    @Singleton
    static class MockDataSourceResolver implements DataSourceResolver {
        @Override
        DataSource resolve(DataSource dataSource) {
            new ReturnedByDataSourceResolver(dataSource)
        }
    }

    static class ReturnedByDataSourceResolver implements DataSource {

        @Delegate
        private final DataSource dataSource

        ReturnedByDataSourceResolver(DataSource dataSource) {
            this.dataSource = dataSource
        }
    }
}
