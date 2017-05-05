package org.zalando.nakadiproducer.transmission.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.fahrschein.NakadiClient;
import org.zalando.nakadiproducer.BaseMockedExternalCommunicationIT;
import org.zalando.nakadiproducer.eventlog.EventLogWriter;
import org.zalando.nakadiproducer.eventlog.EventPayload;
import org.zalando.nakadiproducer.util.Fixture;
import org.zalando.nakadiproducer.util.MockPayload;

public class EventTransmitterIT extends BaseMockedExternalCommunicationIT {
    private static final String MY_EVENT_TYPE = "myEventType";
    private static final String CODE = "code123";

    @Autowired
    private EventLogWriter eventLogWriter;

    @Autowired
    private EventTransmitter eventTransmitter;

    @Autowired
    private NakadiClient nakadiClient;

    @Captor
    private ArgumentCaptor<List<NakadiEvent>> captor;

    @Test
    public void eventsShouldBeSubmittedToNakadi() throws IOException {
        MockPayload code = Fixture.mockPayload(1, CODE);
        EventPayload payload = Fixture.mockEventPayload(code, MY_EVENT_TYPE);
        eventLogWriter.fireCreateEvent(payload);

        eventTransmitter.sendEvents();

        verify(nakadiClient).publish(eq(MY_EVENT_TYPE), captor.capture());
        List<NakadiEvent> value = captor.getValue();

        assertThat(value.size(), is(1));
        assertThat(value.get(0).getDataOperation(), is("C"));
        assertThat(value.get(0).getDataType(), is(payload.getDataType()));
        assertThat(value.get(0).getData().get("code"), is(CODE));
    }
}