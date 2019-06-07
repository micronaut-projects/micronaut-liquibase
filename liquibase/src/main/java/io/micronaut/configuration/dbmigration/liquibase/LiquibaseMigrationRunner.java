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
import io.micronaut.inject.qualifiers.Qualifiers;
import liquibase.resource.ResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
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
}
