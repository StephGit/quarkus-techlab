package ch.puzzle.quarkustechlab.restproducer.boundary;

import ch.puzzle.quarkustechlab.restproducer.control.DummyService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@TestHTTPEndpoint(DataResource.class)
public class RestAssuredDataResourceTest {

    @InjectMock
    DummyService dummyService;

    @Test
    @DisplayName("RestAssured: returns data from /data endpoint of DataResource.class")
    public void testData() {
        when().get()
                .then()
                .statusCode(200)
                .body(CoreMatchers.isA(String.class));
    }

    @Test
    @DisplayName("mock dummyService")
    public void testDummy() {
        Mockito.when(dummyService.dummy()).thenReturn("ima mock");
        when().get("/dummy")
                .then()
                .statusCode(200)
                .body(is("ima mock"));
    }
}
