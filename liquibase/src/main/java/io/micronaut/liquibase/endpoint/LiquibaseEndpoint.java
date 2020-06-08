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

import io.micronaut.liquibase.LiquibaseConfigurationProperties;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import liquibase.changelog.StandardChangeLogHistoryService;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Provides a liquibase endpoint to get all the migrations applied.
 *
 * @author Iván López
 * @since 1.0.0
 */
@Endpoint(id = LiquibaseEndpoint.NAME)
public class LiquibaseEndpoint {

    /**
     * Endpoint name.
     */
    public static final String NAME = "liquibase";

    private static final Logger LOG = LoggerFactory.getLogger(LiquibaseEndpoint.class);
    private final Collection<LiquibaseConfigurationProperties> liquibaseConfigurationProperties;
    private final ApplicationContext applicationContext;

    /**
     * @param liquibaseConfigurationProperties Collection of Liquibase Configurations
     * @param applicationContext               The application context
     */
    public LiquibaseEndpoint(Collection<LiquibaseConfigurationProperties> liquibaseConfigurationProperties, ApplicationContext applicationContext) {
        this.liquibaseConfigurationProperties = liquibaseConfigurationProperties;
        this.applicationContext = applicationContext;
    }

    /**
     * @return A flowable with liquibase changes per active configuration
     */
    @Read
    public Flowable<LiquibaseReport> liquibaseMigrations() {
        return Flowable.create(emitter -> {
            DatabaseFactory factory = DatabaseFactory.getInstance();

            if (liquibaseConfigurationProperties != null) {
                for (LiquibaseConfigurationProperties config : liquibaseConfigurationProperties) {
                    if (config.isEnabled()) {
                        JdbcConnection jdbcConnection = null;

                        try {
                            DataSource dataSource = applicationContext.getBean(DataSource.class, Qualifiers.byName(config.getNameQualifier()));
                            Connection connection = dataSource.getConnection();
                            jdbcConnection = new JdbcConnection(connection);

                            Database database = factory.findCorrectDatabaseImplementation(jdbcConnection);
                            StandardChangeLogHistoryService service = new StandardChangeLogHistoryService();
                            service.setDatabase(database);
                            emitter.onNext(new LiquibaseReport(config.getNameQualifier(), service.getRanChangeSets()));
                        } catch (SQLException | DatabaseException ex) {
                            emitter.onError(ex);
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

            emitter.onComplete();
        }, BackpressureStrategy.BUFFER);
    }
}
