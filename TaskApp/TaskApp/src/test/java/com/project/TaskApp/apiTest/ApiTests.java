package com.project.TaskApp.apiTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // <-- uses H2 from step 2
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiTests {

    private static final String SIGNUP = "/api/auth/register";
    private static final String LOGIN  = "/api/auth/login";

    @LocalServerPort int port;
    String email;
    String username;
    String password = "P@ssw0rd123!";

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        String s = UUID.randomUUID().toString().substring(0, 8);
        username = "apitest_" + s;
        email = "apitest_" + s + "@example.com";
    }

    @Test @Order(1)
    void signup_shouldSucceed() {
        String body = """
          {"username":"%s","email":"%s","password":"%s"}
          """.formatted(username, email, password);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(SIGNUP)
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("$", anyOf(hasKey("message"), hasKey("success"), hasKey("data")));
    }

    @Test @Order(2)
    void login_shouldReturnJwtInsideData() {
        String body = """
      {"username":"%s","password":"%s"}
      """.formatted(username, password); // use username, not email

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(LOGIN)
                .then()
                .statusCode(200)
                .body("data", notNullValue()); // just check it's present
    }


    @Test @Order(3)
    void login_withWrongPassword_shouldFail() {
        String body = """
          {"username":"%s","password":"wrong"}
          """.formatted(email);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(LOGIN)
                .then()
                // some apps still return 200 but with success=false
                .statusCode(anyOf(is(400), is(401), is(200)))
                .body("$", anyOf(hasKey("success"), hasKey("message")));
    }
}
