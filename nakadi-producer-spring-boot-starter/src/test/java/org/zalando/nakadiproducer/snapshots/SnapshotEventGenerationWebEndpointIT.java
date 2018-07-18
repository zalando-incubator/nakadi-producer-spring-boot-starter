package org.zalando.nakadiproducer.snapshots;

import static com.jayway.restassured.RestAssured.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.zalando.nakadiproducer.TestApplication;
import org.zalando.nakadiproducer.config.EmbeddedDataSourceConfig;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
            "management.security.enabled=false",
            "zalando.team.id:alpha-local-testing",
            "nakadi-producer.scheduled-transmission-enabled:false",
            "management.endpoints.web.exposure.include:snapshot-event-creation"
    },
    classes = { TestApplication.class, EmbeddedDataSourceConfig.class, SnapshotEventGenerationWebEndpointIT.Config.class }
)
public class SnapshotEventGenerationWebEndpointIT {

    private static final String MY_EVENT_TYPE = "my.event-type";
    private static final String FILTER = "myRequestBody";

    @LocalManagementPort
    private int managementPort;

    @Autowired
    private SnapshotEventGenerator snapshotEventGenerator;

    @Before
    public void resetMocks() {
        reset(snapshotEventGenerator);
    }

    @Test
    public void passesFilterIfPresentInUrl() {
        given().baseUri("http://localhost:" + managementPort)
                .contentType("application/json")
        .when().post("/actuator/snapshot-event-creation/" + MY_EVENT_TYPE + "?filter=" + FILTER)
        .then().statusCode(204);

        verify(snapshotEventGenerator).generateSnapshots(null, FILTER);
    }

    @Test
    public void passesFilterIfPresentInBody() {
        given().baseUri("http://localhost:" + managementPort)
                .contentType("application/json")
                .body("{\"filter\":\"" + FILTER + "\"}")
        .when().post("/actuator/snapshot-event-creation/" + MY_EVENT_TYPE)
        .then().statusCode(204);

        verify(snapshotEventGenerator).generateSnapshots(null, FILTER);
    }

    @Test
    public void passesNullIfNoFilterIsPresent() {
        given().baseUri("http://localhost:" + managementPort)
               .contentType("application/json")
               .when().post("/actuator/snapshot-event-creation/" + MY_EVENT_TYPE)
               .then().statusCode(204);

        verify(snapshotEventGenerator).generateSnapshots(null, null);
    }

    @Configuration
    public static class Config {
        @Bean
        public SnapshotEventGenerator snapshotEventGenerator() {
            SnapshotEventGenerator mock = mock(SnapshotEventGenerator.class);
            when(mock.getSupportedEventType()).thenReturn(MY_EVENT_TYPE);
            return mock;
        }

    }
}
