/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.configuration.dbmigration.liquibase;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.naming.NameResolver;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.runtime.exceptions.ApplicationStartupException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.Async;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Run Liquibase migrations when there is a {@link DataSource} defined.
 *
 * @author Sergio del Amo
 * @author Iván López
 * @since 1.0.0
 */
@Singleton
class LiquibaseMigrationRunner extends AbstractLiquibaseMigration implements BeanCreatedEventListener<DataSource> {

    private static final Logger LOG = LoggerFactory.getLogger(LiquibaseMigrationRunner.class);

    /**
     * @param applicationContext The application context
     * @param resourceAccessor   An implementation of {@link liquibase.resource.ResourceAccessor}
     */
    LiquibaseMigrationRunner(ApplicationContext applicationContext, ResourceAccessor resourceAccessor) {
        super(applicationContext, resourceAccessor);
    }

    @Override
    public DataSource onCreated(BeanCreatedEvent<DataSource> event) {
        DataSource dataSource = event.getBean();

        if (event.getBeanDefinition() instanceof NameResolver) {
            ((NameResolver) event.getBeanDefinition())
                    .resolveName()
                    .ifPresent(name -> {
                        applicationContext
                                .findBean(LiquibaseConfigurationProperties.class, Qualifiers.byName(name))
                                .ifPresent(liquibaseConfig -> run(liquibaseConfig, dataSource));
                    });
        }

        return dataSource;
    }

    /**
     * Run Liquibase migration for a specific config and a dataSource.
     *
     * @param config     The {@link LiquibaseConfigurationProperties}
     * @param dataSource The {@link DataSource}
     */
    void run(LiquibaseConfigurationProperties config, DataSource dataSource) {
        if (config.isEnabled()) {
            if (config.isAsync()) {
                migrateAsync(config, dataSource);
            } else {
                migrate(config, dataSource);
            }
        }
    }

    /**
     * Run a migration asynchronously.
     *
     * @param config     The {@link LiquibaseConfigurationProperties}
     * @param dataSource The {@link DataSource}
     */
    @Async(TaskExecutors.IO)
    void migrateAsync(LiquibaseConfigurationProperties config, DataSource dataSource) {
        migrate(config, dataSource);
    }

    /**
     * Performs liquibase update for the given data datasource and configuration.
     *
     * @param config     The {@link LiquibaseConfigurationProperties}
     * @param dataSource The {@link DataSource}
     */
    private void migrate(LiquibaseConfigurationProperties config, DataSource dataSource) {
        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Migration failed! Could not connect to the datasource.", e);
            }
            throw new ApplicationStartupException("Migration failed! Could not connect to the datasource.", e);
        }

        Liquibase liquibase = null;
        try {
            liquibase = createLiquibase(connection, config);
            generateRollbackFile(liquibase, config);
            performUpdate(liquibase, config);
        } catch (LiquibaseException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Migration failed! Liquibase encountered an exception.", e);
            }
            throw new ApplicationStartupException("Migration failed! Liquibase encountered an exception.", e);
        } finally {
            Database database = null;
            if (liquibase != null) {
                database = liquibase.getDatabase();
            }
            if (database != null) {
                try {
                    database.close();
                } catch (DatabaseException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Error closing the connection after the migration.", e);
                    }
                }
            }
        }
    }

    /**
     * Performs Liquibase update.
     *
     * @param liquibase Primary facade class for interacting with Liquibase.
     * @param config    Liquibase configuration
     * @throws LiquibaseException Liquibase exception.
     */
    private void performUpdate(Liquibase liquibase, LiquibaseConfigurationProperties config) throws LiquibaseException {
        LabelExpression labelExpression = new LabelExpression(config.getLabels());
        Contexts contexts = new Contexts(config.getContexts());
        if (config.isTestRollbackOnUpdate()) {
            if (config.getTag() != null) {
                liquibase.updateTestingRollback(config.getTag(), contexts, labelExpression);
            } else {
                liquibase.updateTestingRollback(contexts, labelExpression);
            }
        } else {
            if (config.getTag() != null) {
                liquibase.update(config.getTag(), contexts, labelExpression);
            } else {
                liquibase.update(contexts, labelExpression);
            }
        }
    }

    /**
     * Generates Rollback file.
     *
     * @param liquibase Primary facade class for interacting with Liquibase.
     * @param config    Liquibase configuration
     * @throws LiquibaseException Liquibase exception.
     */
    private void generateRollbackFile(Liquibase liquibase, LiquibaseConfigurationProperties config) throws LiquibaseException {
        if (config.getRollbackFile() != null) {
            String outputEncoding = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class).getOutputEncoding();
            try (FileOutputStream fileOutputStream = new FileOutputStream(config.getRollbackFile());
                 Writer output = new OutputStreamWriter(fileOutputStream, outputEncoding)) {
                Contexts contexts = new Contexts(config.getContexts());
                LabelExpression labelExpression = new LabelExpression(config.getLabels());
                if (config.getTag() != null) {
                    liquibase.futureRollbackSQL(config.getTag(), contexts, labelExpression, output);
                } else {
                    liquibase.futureRollbackSQL(contexts, labelExpression, output);
                }
            } catch (IOException e) {
                throw new LiquibaseException("Unable to generate rollback file.", e);
            }
        }
    }

    /**
     * @param connection Connection with the data source
     * @param config     Liquibase Configuration for the Data source
     * @return A Liquibase object
     * @throws LiquibaseException A liquibase exception.
     */
    private Liquibase createLiquibase(Connection connection, LiquibaseConfigurationProperties config) throws LiquibaseException {
        String changeLog = config.getChangeLog();
        Database database = createDatabase(connection, resourceAccessor, config);
        Liquibase liquibase = new Liquibase(changeLog, resourceAccessor, database);
        liquibase.setIgnoreClasspathPrefix(config.isIgnoreClasspathPrefix());
        if (config.getParameters() != null) {
            for (Map.Entry<String, String> entry : config.getParameters().entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }
        }

        if (config.isDropFirst()) {
            liquibase.dropAll();
        }

        applicationContext.registerSingleton(Liquibase.class, liquibase, Qualifiers.byName(config.getNameQualifier()), false);

        return liquibase;
    }

    /**
     * Subclasses may override this method add change some database settings such as
     * default schema before returning the database object.
     *
     * @param connection       Connection with the data source
     * @param resourceAccessor Abstraction of file access
     * @param config           Liquibase Configuration for the Data source
     * @return a Database implementation retrieved from the {@link DatabaseFactory}.
     * @throws DatabaseException A Liquibase Database exception.
     */
    private Database createDatabase(Connection connection,
                                    ResourceAccessor resourceAccessor,
                                    LiquibaseConfigurationProperties config) throws DatabaseException {

        DatabaseConnection liquibaseConnection;
        if (connection == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Null connection returned by liquibase datasource. Using offline unknown database");
            }
            liquibaseConnection = new OfflineConnection("offline:unknown", resourceAccessor);

        } else {
            liquibaseConnection = new JdbcConnection(connection);
        }

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(liquibaseConnection);
        String defaultSchema = config.getDefaultSchema();
        if (StringUtils.isNotEmpty(defaultSchema)) {
            if (database.supportsSchemas()) {
                database.setDefaultSchemaName(defaultSchema);
            } else if (database.supportsCatalogs()) {
                database.setDefaultCatalogName(defaultSchema);
            }
        }
        String liquibaseSchema = config.getLiquibaseSchema();
        if (StringUtils.isNotEmpty(liquibaseSchema)) {
            if (database.supportsSchemas()) {
                database.setLiquibaseSchemaName(liquibaseSchema);
            } else if (database.supportsCatalogs()) {
                database.setLiquibaseCatalogName(liquibaseSchema);
            }
        }
        if (trimToNull(config.getLiquibaseTablespace()) != null && database.supportsTablespaces()) {
            database.setLiquibaseTablespaceName(config.getLiquibaseTablespace());
        }
        if (trimToNull(config.getDatabaseChangeLogTable()) != null) {
            database.setDatabaseChangeLogTableName(config.getDatabaseChangeLogTable());
        }
        if (trimToNull(config.getDatabaseChangeLogLockTable()) != null) {
            database.setDatabaseChangeLogLockTableName(config.getDatabaseChangeLogLockTable());
        }
        return database;
    }

    private static String trimToNull(String string) {
        if (string == null) {
            return null;
        }
        String returnString = string.trim();
        if (returnString.isEmpty()) {
            return null;
        } else {
            return returnString;
        }
    }
}
