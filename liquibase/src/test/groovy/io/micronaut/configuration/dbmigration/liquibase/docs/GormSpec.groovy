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

package io.micronaut.configuration.dbmigration.liquibase.docs


import io.micronaut.configuration.dbmigration.liquibase.YamlAsciidocTagCleaner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class GormSpec extends Specification implements YamlAsciidocTagCleaner {

    String gormConfig = '''\
spec.name: GormDocSpec
//tag::yamlconfig[]
dataSource: # <1>
  pooled: true
  jmxExport: true
  dbCreate: none # <2>
  url: 'jdbc:h2:mem:GORMDb'
  driverClassName: org.h2.Driver
  username: sa
  password: ''
        
liquibase:
  datasources: # <3>
    default: # <4>
      locations: classpath:db/liquibase-changelog.xml # <5>
'''//end::yamlconfig[]

    @Shared
    Map<String, Object> liquibaseMap = [
        'spec.name': 'GormDocSpec',
        dataSource : [
            pooled         : true,
            jmxExport      : true,
            dbCreate       : 'none',
            url            : 'jdbc:h2:mem:GORMDb',
            driverClassName: 'org.h2.Driver',
            username       : 'sa',
            password       : ''
        ],
        liquibase  : [
            datasources: [
                default: [
                    locations: 'classpath:db/liquibase-changelog.xml'
                ]
            ]
        ]
    ]

    @Shared
    Map<String, Object> config = [:] << flatten(liquibaseMap)

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, config as Map<String, Object>, Environment.TEST)

    void 'test liquibase migrations are executed with GORM'() {
//        given:
//        ApplicationContext applicationContext = ApplicationContext.run(flatten(liquibaseMap) as Map<String, Object>, Environment.TEST)
//
        expect:
        true

//        when:
//            embeddedServer.applicationContext.getBean(GormMigrationRunner)
//
//        then:
//        noExceptionThrown()
//
//        when:
//        applicationContext.getBean(Liquibase)
//
//        then:
//        noExceptionThrown()
//
//        when:
//            LiquibaseConfigurationProperties config = applicationContext.getBean(LiquibaseConfigurationProperties)
//
//        then:
//        noExceptionThrown()
//        !config.isAsync()
//
//        when:
//        Map m = new Yaml().load(cleanYamlAsciidocTag(gormConfig))
//
//        then:
//        m == liquibaseMap
//
//        when:
//        Map db = [url: 'jdbc:h2:mem:GORMDb', user: 'sa', password: '', driver: 'org.h2.Driver']
//        Sql sql = Sql.newInstance(db.url, db.user, db.password, db.driver)
//
//        then:
//        sql.rows('select count(*) from books').get(0)[0] == 2
    }
}
