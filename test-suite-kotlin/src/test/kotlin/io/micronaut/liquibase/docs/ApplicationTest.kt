package io.micronaut.liquibase.docs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.bridge.SLF4JBridgeHandler
import java.sql.DriverManager

class ApplicationTest {

    @Test
    fun theApplicationBridgesJulToSlf4jAndRunsTheMigrations() {
        assertFalse(SLF4JBridgeHandler.isInstalled())

        Application.main(emptyArray())

        assertTrue(SLF4JBridgeHandler.isInstalled())

        // the Liquibase migrations of application.yml ran against the in-memory database
        DriverManager.getConnection("jdbc:h2:mem:liquibaseDocsDb;DB_CLOSE_DELAY=-1", "sa", "").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("select count(*) from DATABASECHANGELOG where EXECTYPE = 'EXECUTED'").use { resultSet ->
                    assertTrue(resultSet.next())
                    assertEquals(2, resultSet.getInt(1))
                }
            }
        }
    }
}
