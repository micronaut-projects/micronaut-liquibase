package io.micronaut.liquibase.docs

import groovy.sql.Sql
import org.slf4j.bridge.SLF4JBridgeHandler
import spock.lang.Specification

class ApplicationSpec extends Specification {

    void "the application bridges JUL to Slf4j and runs the migrations"() {
        expect:
        !SLF4JBridgeHandler.isInstalled()

        when:
        Application.main(new String[0])

        then:
        SLF4JBridgeHandler.isInstalled()

        when: "the Liquibase migrations of application.yml ran against the in-memory database"
        def sql = Sql.newInstance("jdbc:h2:mem:liquibaseDocsDb;DB_CLOSE_DELAY=-1", "sa", "")
        def row = sql.firstRow("select count(*) as executed from DATABASECHANGELOG where EXECTYPE = 'EXECUTED'")

        then:
        row.executed == 2

        cleanup:
        sql?.close()
    }
}
