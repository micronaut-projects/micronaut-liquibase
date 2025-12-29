package micronaut.example;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@DisabledInNativeImage
@MicronautTest
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return Postgresql.getProperties();
    }

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
