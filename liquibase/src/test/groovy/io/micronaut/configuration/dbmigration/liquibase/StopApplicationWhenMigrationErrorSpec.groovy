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

package io.micronaut.configuration.dbmigration.liquibase

import groovy.sql.Sql
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.sql.DataSource

class StopApplicationWhenMigrationErrorSpec extends Specification {

    @Shared
    Map<String, Object> config = [
        'datasources.default.url'                      : 'jdbc:h2:mem:liquibaseExistingDb',
        'datasources.default.username'                 : 'sa',
        'datasources.default.password'                 : '',
        'datasources.default.driverClassName'          : 'org.h2.Driver',

        'jpa.default.packages-to-scan'                 : ['example.micronaut'],
        'jpa.default.properties.hibernate.hbm2ddl.auto': 'none',
        'jpa.default.properties.hibernate.show_sql'    : true,

        'liquibase.datasources.default.async'          : false,
        'liquibase.datasources.default.change-log'     : 'classpath:db/liquibase-changelog.xml',
    ]

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, config as Map<String, Object>, Environment.TEST)

    void "test application context stops if there is an error with the migrations"() {
        given: 'an existing table in the database'
        Map db = [url: 'jdbc:h2:mem:liquibaseExistingDb', user: 'sa', password: '', driver: 'org.h2.Driver']
        Sql sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
        sql.execute('create table books(id int not null primary key, name varchar(255));')

        when:
        embeddedServer.applicationContext.getBean(DataSource)

        then:
        noExceptionThrown()

        when:
        embeddedServer.applicationContext.getBean(LiquibaseConfigurationProperties)

        then:
        PollingConditions conditions = new PollingConditions(timeout: 5)
        conditions.eventually {
            !embeddedServer.applicationContext.isRunning() &&
                sql.rows('select count(*) from books').get(0)[0] == 0
        }
    }
}
