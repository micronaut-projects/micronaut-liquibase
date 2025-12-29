package micronaut.example;

import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * @see <a href="https://testcontainers.com/modules/postgresql/">postgresql TestContainers</a>
 */
public class Postgresql {
    private static final String IMAGE_NAME = "postgres";
    private static PostgreSQLContainer container;

    public static Map<String, String> getProperties() {
        if (container == null) {
            container = new PostgreSQLContainer(DockerImageName.parse(IMAGE_NAME));
            container.start();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while(!container.isRunning());
            return getProperties(container);
        } else {
            return getProperties(container);
        }
    }

    private static Map<String, String> getProperties(PostgreSQLContainer container) {
        return Map.of(
            "datasources.default.url", container.getJdbcUrl(),
            "datasources.default.username", container.getUsername(),
            "datasources.default.password", container.getPassword(),
            "datasources.default.db-type", "postgres",
            "datasources.default.driver-class-name", "org.postgresql.Driver"
        );
    }
}
