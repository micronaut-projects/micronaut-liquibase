package io.micronaut.liquibase;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.jdbc.DataSourceResolver;
import liquibase.resource.ResourceAccessor;

import javax.inject.Singleton;
import javax.sql.DataSource;

/**
 * Public access to invoke Liquibase migrations when DataSource onCreate behaviour is not desired.
 *
 * The following Liquibase property should be disabled
 *    liquibase.datasources.*.enabled = false
 *
 * This ensures that Liquibase wont automatically migrate. The following service can then be injected later
 * and either forceRun or safeRun a migration based on a given LiquibaseConfigurationProperties
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
	public LiquibaseMigrator(ApplicationContext applicationContext, ResourceAccessor resourceAccessor, @Nullable DataSourceResolver dataSourceResolver) {
		super(applicationContext, resourceAccessor, dataSourceResolver);
	}

	/**
	 * Safe run Liquibase migration for a specific config and a dataSource.
	 *
	 * @param config     The {@link LiquibaseConfigurationProperties}
	 * @param dataSource The {@link DataSource}
	 */
	public void safeRun(LiquibaseConfigurationProperties config, DataSource dataSource){
		super.run(config, dataSource);
	}

	/**
	 * Force run Liquibase migration for a specific config and a dataSource.
	 *
	 * @param config     The {@link LiquibaseConfigurationProperties}
	 * @param dataSource The {@link DataSource}
	 */
	public void forceRun(LiquibaseConfigurationProperties config, DataSource dataSource){
		if(!config.isEnabled())
			config.setEnabled(true);
		super.run(config, dataSource);
	}

}
