package io.micronaut.liquibase.docs

import groovy.sql.Sql
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.liquibase.YamlAsciidocTagCleaner
import org.yaml.snakeyaml.Yaml
import spock.lang.Shared
import spock.lang.Specification

class GormSpec extends Specification implements YamlAsciidocTagCleaner {

    String gormConfig = '''\
spec.name: GormDocSpec
//tag::yamlconfig[]
dataSource:
  pooled: true
  jmxExport: true
  dbCreate: none
  url: 'jdbc:h2:mem:GORMDb'
  driverClassName: org.h2.Driver
  username: sa
  password: ''

liquibase:
  datasources:
    default:
      change-log: classpath:db/liquibase-changelog.xml
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
