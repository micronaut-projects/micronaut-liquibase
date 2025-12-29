package micronaut.example;

import org.testcontainers.mariadb.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

/**
 * @see <a href="https://testcontainers.com/modules/mariadb/">MariaDB TestContainers</a>
 */
public class MariaDb {
    private static final String IMAGE_NAME = "mariadb";
    private static MariaDBContainer container;

    public static Map<String, String> getProperties() {
        if (container == null) {
            container = new MariaDBContainer(DockerImageName.parse(IMAGE_NAME));
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

    private static Map<String, String> getProperties(MariaDBContainer container) {
        return Map.of(
            "datasources.default.url", container.getJdbcUrl(),
            "datasources.default.username", container.getUsername(),
            "datasources.default.password", container.getPassword(),
            "datasources.default.db-type", "mariadb",
            "datasources.default.driver-class-name", "org.mariadb.jdbc.Driver"
        );
    }
}
