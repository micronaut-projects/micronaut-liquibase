package micronaut.example;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.extensions.junit5.annotation.TestResourcesScope;
import jakarta.inject.Inject;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@MicronautTest
@TestResourcesScope("postgres-scope")
class UserControllerTest {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void users() throws JSONException {
        BlockingHttpClient client = httpClient.toBlocking();
        String actual = client.retrieve(HttpRequest.GET("/users"));
        String expected = "[{\"id\":1,\"username\":\"ilopmar\",\"firstName\":\"Iván\",\"lastName\":\"López\"},{\"id\":2,\"username\":\"graemerocher\",\"firstName\":\"Graeme\",\"lastName\":\"Rocher\"}]";
        JSONAssert.assertEquals(
            expected, actual, JSONCompareMode.LENIENT);
    }
}
