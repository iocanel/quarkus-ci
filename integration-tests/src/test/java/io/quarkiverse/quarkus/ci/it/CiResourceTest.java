package io.quarkiverse.quarkus.ci.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CiResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/ci")
                .then()
                .statusCode(200)
                .body(is("Hello ci"));
    }
}
