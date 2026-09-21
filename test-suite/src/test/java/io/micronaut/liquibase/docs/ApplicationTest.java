package io.micronaut.liquibase.docs;

import org.junit.jupiter.api.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationTest {

    @Test
    void theApplicationBridgesJulToSlf4jAndRunsTheMigrations() throws SQLException {
        assertFalse(SLF4JBridgeHandler.isInstalled());

        Application.main(new String[0]);

        assertTrue(SLF4JBridgeHandler.isInstalled());

        // the Liquibase migrations of application.yml ran against the in-memory database
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:liquibaseDocsDb;DB_CLOSE_DELAY=-1", "sa", "");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("select count(*) from DATABASECHANGELOG where EXECTYPE = 'EXECUTED'")) {
            assertTrue(resultSet.next());
            assertEquals(2, resultSet.getInt(1));
        }
    }
}
