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
package io.micronaut.liquibase.endpoint;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.jdbc.DataSourceResolver;
import io.micronaut.liquibase.LiquibaseConfigurationProperties;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import io.micronaut.serde.annotation.SerdeImport;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.StandardChangeLogHistoryService;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Provides a liquibase endpoint to get all the migrations applied.
 *
 * @author Iván López
 * @since 1.0.0
 */
@SerdeImport(RanChangeSet.class)
@Endpoint(id = LiquibaseEndpoint.NAME)
public class LiquibaseEndpoint {

    /**
     * Endpoint name.
     */
    public static final String NAME = "liquibase";

    private static final Logger LOG = LoggerFactory.getLogger(LiquibaseEndpoint.class);
    private final Collection<LiquibaseConfigurationProperties> liquibaseConfigurationProperties;
    private final ApplicationContext applicationContext;
    private final DataSourceResolver dataSourceResolver;

    /**
     * @param liquibaseConfigurationProperties Collection of Liquibase Configurations
     * @param applicationContext               The application context
     *
     * @deprecated Use {@link #LiquibaseEndpoint(Collection, ApplicationContext, DataSourceResolver)} instead
     */
    @Deprecated
    public LiquibaseEndpoint(Collection<LiquibaseConfigurationProperties> liquibaseConfigurationProperties,
                             ApplicationContext applicationContext) {
        this(liquibaseConfigurationProperties, applicationContext, null);
    }

    /**
     * @param liquibaseConfigurationProperties Collection of Liquibase Configurations
     * @param applicationContext               The application context
     * @param dataSourceResolver               The data source resolver
     */
    @Creator
    public LiquibaseEndpoint(Collection<LiquibaseConfigurationProperties> liquibaseConfigurationProperties,
                             ApplicationContext applicationContext,
                             @Nullable DataSourceResolver dataSourceResolver) {
        this.liquibaseConfigurationProperties = liquibaseConfigurationProperties;
        this.applicationContext = applicationContext;
        this.dataSourceResolver = dataSourceResolver;
    }

    /**
     * @return A publisher with liquibase changes per active configuration
     */
    @Read
    public Publisher<LiquibaseReport> liquibaseMigrations() {
        return Flux.create(emitter -> {
            DatabaseFactory factory = DatabaseFactory.getInstance();

            if (liquibaseConfigurationProperties != null) {
                for (LiquibaseConfigurationProperties config : liquibaseConfigurationProperties) {
                    if (config.isEnabled()) {
                        JdbcConnection jdbcConnection = null;

                        try {
                            DataSource dataSource = applicationContext.getBean(DataSource.class, Qualifiers.byName(config.getNameQualifier()));
                            if (dataSourceResolver != null) {
                                dataSource = dataSourceResolver.resolve(dataSource);
                            }
                            jdbcConnection = new JdbcConnection(dataSource.getConnection());

                            Database database = factory.findCorrectDatabaseImplementation(jdbcConnection);
                            StandardChangeLogHistoryService service = new StandardChangeLogHistoryService();
                            service.setDatabase(database);
                            emitter.next(new LiquibaseReport(config.getNameQualifier(), service.getRanChangeSets()));
                        } catch (SQLException | DatabaseException ex) {
                            emitter.error(ex);
                        } finally {
                            if (jdbcConnection != null) {
                                try {
                                    jdbcConnection.close();
                                } catch (DatabaseException e) {
                                    if (LOG.isWarnEnabled()) {
                                        LOG.warn("Failed to close a connection to the liquibase datasource", e);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            emitter.complete();
        }, FluxSink.OverflowStrategy.BUFFER);
    }
}
