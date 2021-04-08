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
import io.micronaut.core.annotation.Nullable;
import io.micronaut.jdbc.DataSourceResolver;
import liquibase.resource.ResourceAccessor;

import javax.inject.Singleton;
import javax.sql.DataSource;

/**
 * Public access to invoke Liquibase migrations when DataSource onCreate behaviour is not desired.
 * <p>
 * The following Liquibase property should be disabled:
 *      liquibase.datasources.*.enabled = false
 * <p>
 * This ensures that Liquibase won't run automatically the migrations. The following service can then be injected later
 * and either forceRun or safeRun a migration based on a given {@link LiquibaseConfigurationProperties}.
 *
 * @author Kevin Jouper
 * @since 2.4.0
 */
@Singleton
public class LiquibaseMigrator extends LiquibaseMigrationRunner {

    /**
     * @param applicationContext The application context
     * @param resourceAccessor   An implementation of {@link ResourceAccessor}
     * @param dataSourceResolver The data source resolver
     */
    public LiquibaseMigrator(ApplicationContext applicationContext,
                             ResourceAccessor resourceAccessor,
                             @Nullable DataSourceResolver dataSourceResolver) {
        super(applicationContext, resourceAccessor, dataSourceResolver);
    }

    /**
     * Safe run Liquibase migration for a specific config and a DataSource.
     *
     * @param config     The {@link LiquibaseConfigurationProperties}
     * @param dataSource The {@link DataSource}
     */
    public void safeRun(LiquibaseConfigurationProperties config,
                        DataSource dataSource) {
        super.run(config, dataSource);
    }

    /**
     * Force run Liquibase migration for a specific config and a DataSource.
     *
     * @param config     The {@link LiquibaseConfigurationProperties}
     * @param dataSource The {@link DataSource}
     */
    public void forceRun(LiquibaseConfigurationProperties config,
                         DataSource dataSource) {
        if (!config.isEnabled()) {
            config.setEnabled(true);
        }
        super.run(config, dataSource);
    }

}
