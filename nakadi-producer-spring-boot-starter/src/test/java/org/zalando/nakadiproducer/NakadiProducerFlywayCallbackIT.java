package org.zalando.nakadiproducer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

public class NakadiProducerFlywayCallbackIT extends BaseMockedExternalCommunicationIT {

    @MockBean
    private NakadiProducerFlywayCallback flywayCallback;

    @MockBean
    private ConfigurationAwareNakadiProducerFlywayCallback configurationAwareNakadiProducerFlywayCallback;

    @Test
    @DirtiesContext // Needed to make sure that flyway gets executed for each of the tests and Callbacks are called again
    public void flywayCallbackIsCalledIfAnnotatedWithQualifierAnnotation() {
        verify(flywayCallback, times(1)).beforeMigrate(any(Connection.class));
    }

    @Test
    @DirtiesContext // Needed to make sure that flyway gets executed for each of the tests and Callbacks are called again
    public void flywayConfigurationIsSetIfCallbackIsConfigurationAware() {
        verify(configurationAwareNakadiProducerFlywayCallback).setFlywayConfiguration(any(FlywayConfiguration.class));
    }

    public interface ConfigurationAwareNakadiProducerFlywayCallback extends NakadiProducerFlywayCallback, ConfigurationAware {
    }
}
