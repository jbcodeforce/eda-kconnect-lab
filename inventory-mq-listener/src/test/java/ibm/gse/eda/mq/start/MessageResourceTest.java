package ibm.gse.eda.mq.start;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MessageResourceTest {

    @BeforeAll
    public static void startDependencies(){

    }

    @Test
    public void testGetConfigEndpoint() {
        

        given()
          .when().get("/mqdemo/config")
          .then()
             .statusCode(200)
             .body("qmgr",equalTo("QM1"));
    }

}