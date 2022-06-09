/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.liquibase;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.naming.NameResolver;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.jdbc.DataSourceResolver;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.Async;
import jakarta.inject.Singleton;
import liquibase.resource.ResourceAccessor;

import javax.sql.DataSource;

/**
 * Run Liquibase migrations when there is a {@link DataSource} defined.
 *
 * @author Sergio del Amo
 * @author Iván López
 * @since 1.0.0
 */
@Singleton
class LiquibaseMigrationRunner extends AbstractLiquibaseMigration implements BeanCreatedEventListener<DataSource> {

    private final DataSourceResolver dataSourceResolver;

    /**
     * @param applicationContext The application context
     * @param resourceAccessor   An implementation of {@link ResourceAccessor}
     * @param dataSourceResolver The data source resolver
     */
    LiquibaseMigrationRunner(ApplicationContext applicationContext,
                             ResourceAccessor resourceAccessor,
                             @Nullable DataSourceResolver dataSourceResolver) {
        super(applicationContext, resourceAccessor);
        this.dataSourceResolver = dataSourceResolver != null ? dataSourceResolver : DataSourceResolver.DEFAULT;
    }

    @Override
    public DataSource onCreated(BeanCreatedEvent<DataSource> event) {
        DataSource dataSource = event.getBean();
        if (event.getBeanDefinition() instanceof NameResolver) {
            ((NameResolver) event.getBeanDefinition())
                    .resolveName().flatMap(name -> applicationContext
                            .findBean(LiquibaseConfigurationProperties.class, Qualifiers.byName(name))).ifPresent(cfg -> {
                        DataSource unwrappedDataSource = dataSourceResolver.resolve(dataSource);
                        run(cfg, unwrappedDataSource);
                    });
        }
        return dataSource;
    }

    /**
     * Run a migration asynchronously.
     *
     * @param config     The {@link LiquibaseConfigurationProperties}
     * @param dataSource The {@link DataSource}
     */
    @Async(TaskExecutors.IO)
    @Override
    void migrateAsync(LiquibaseConfigurationProperties config, DataSource dataSource) {
        super.migrateAsync(config, dataSource);
    }

}
