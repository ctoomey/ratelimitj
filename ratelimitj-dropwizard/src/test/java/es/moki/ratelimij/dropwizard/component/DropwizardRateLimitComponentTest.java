package es.moki.ratelimij.dropwizard.component;

import es.moki.ratelimij.dropwizard.component.app.RateLimitApplication;
import es.moki.ratelimij.dropwizard.component.app.model.LoginRequest;
import io.dropwizard.Configuration;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.stream.IntStream;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(JUnitPlatform.class)
public class DropwizardRateLimitComponentTest {

    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE =
            new DropwizardAppRule<>(RateLimitApplication.class, ResourceHelpers.resourceFilePath("ratelimit-app.yml"));


//    @BeforeAll
//    public static void beforeAll() {
//        client = RedisClient.create("redis://localhost");
//        connect = client.connect();
//    }
//
//    @AfterAll
//    public static void afterAll() {
//        connect.close();
//        client.shutdown();
//    }
//
//    @AfterEach
//    public void afterEach() {
//        try (StatefulRedisConnection<String, String> connection = client.connect()) {
//            connection.sync().flushdb();
//        }
//    }

    @Test
    public void loginHandlerRedirectsAfterPost() {
        final RestClient client = new RestClient();

        IntStream.rangeClosed(1, 2)
                .forEach(i -> assertThat(client.get().getStatus()).isEqualTo(200));

        IntStream.rangeClosed(1, 5)
                .forEach(i -> assertThat(client.login().getStatus()).isEqualTo(200));

        assertThat(client.login().getStatus()).isEqualTo(429);

        IntStream.rangeClosed(1, 3)
                .forEach(i -> assertThat(client.get().getStatus()).isEqualTo(200));

        assertThat(client.get().getStatus()).isEqualTo(429);
    }

    private static class RestClient {

        private final Client client = ClientBuilder.newBuilder().build();

        Response login() {
            return client.target(
                    String.format("http://localhost:%d/application/login", RULE.getLocalPort()))
                    .request()
                    .post(Entity.json(loginForm()));
        }

        Response get() {
            return client.target(
                    String.format("http://localhost:%d/application/user/{id}", RULE.getLocalPort()))
                    .resolveTemplate("id", 1)
                    .request()
                    .get();
        }

        private LoginRequest loginForm() {
            return new LoginRequest("heisenberg", "abc123");
        }
    }

}