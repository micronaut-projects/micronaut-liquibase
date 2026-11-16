package micronaut.example;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
class UserControllerTest {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Inject
    DataSource dataSource;

    @Test
    void users() throws JSONException {
        BlockingHttpClient client = httpClient.toBlocking();
        String actual = client.retrieve(HttpRequest.GET("/users"));
        String expected = "[{\"id\":1,\"username\":\"ilopmar\",\"firstName\":\"Iván\",\"lastName\":\"López\"},{\"id\":2,\"username\":\"graemerocher\",\"firstName\":\"Graeme\",\"lastName\":\"Rocher\"}]";
        JSONAssert.assertEquals(
            expected, actual, JSONCompareMode.LENIENT);
    }

    @Test
    void executesSqlFileChangeInNativeImage() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT marker FROM native_image_sql_file_test")) {
            assertTrue(resultSet.next());
            assertEquals("executed", resultSet.getString("marker"));
        }
    }
}
