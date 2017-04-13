package net.knasmueller.pathfinder.apiTests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class ApiTest {

    @LocalServerPort
    int localPort;

    @Before
    public void init() {
        baseURI = "http://localhost";
        port = localPort;

    }

    @Test
    public void test_circuitClosed_getResult() {
        get("/circuitbreaker/close");
        get("/users").then().body("[0].id", equalTo(1));
    }

    @Test
    public void test_circuitOpen_failFast() {
        get("/circuitbreaker/open");
        get("/users").then().body("status", equalTo("Circuit open"));
    }



}
