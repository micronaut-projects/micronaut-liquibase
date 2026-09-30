from java.sql import DriverManager
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test
from org.slf4j.bridge import SLF4JBridgeHandler

from .Application import Application


@MicronautTest(startApplication=False)
class ApplicationTest:

    @Test
    def the_application_bridges_jul_to_slf4j_and_runs_the_migrations(self) -> None:
        assert not SLF4JBridgeHandler.isInstalled()

        Application.main([])

        assert SLF4JBridgeHandler.isInstalled()

        # the Liquibase migrations of application.yml ran against the in-memory database
        connection = DriverManager.getConnection("jdbc:h2:mem:liquibaseDocsDb;DB_CLOSE_DELAY=-1", "sa", "")
        try:
            statement = connection.createStatement()
            result_set = statement.executeQuery("select count(*) from DATABASECHANGELOG where EXECTYPE = 'EXECUTED'")
            assert result_set.next()
            assert result_set.getInt(1) == 2
        finally:
            connection.close()
