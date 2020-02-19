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

import groovy.sql.Sql
import io.micronaut.configuration.dbmigration.liquibase.YamlAsciidocTagCleaner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.runtime.server.EmbeddedServer
import org.yaml.snakeyaml.Yaml
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
      change-log: classpath:db/liquibase-changelog.xml # <5>
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
                    'change-log': 'classpath:db/liquibase-changelog.xml'
                ]
            ]
        ]
    ]

    void 'test liquibase migrations are executed with GORM'() {
        given:
        def ctx = ApplicationContext.run(flatten(liquibaseMap), Environment.TEST)

        when:
        Map m = new Yaml().load(cleanYamlAsciidocTag(gormConfig))

        then:
        m == liquibaseMap

        when:
        Map db = [url: 'jdbc:h2:mem:GORMDb', user: 'sa', password: '', driver: 'org.h2.Driver']
        Sql sql = Sql.newInstance(db.url, db.user, db.password, db.driver)

        then:
        sql.rows('select count(*) from books').get(0)[0] == 2

        cleanup:
        ctx.close()
    }
}
