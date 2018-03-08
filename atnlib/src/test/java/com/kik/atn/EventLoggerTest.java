package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventLoggerTest {

    @Mock
    private ATNServer mockAtnServer;
    @Mock
    private AndroidLogger mockAndroidLogger;
    private EventLogger eventLogger;
    private String expectedPublicAddress;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        eventLogger = new EventLogger(mockAtnServer, mockAndroidLogger, false);
        expectedPublicAddress = "GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV";

        eventLogger.setPublicAddress(expectedPublicAddress);
    }

    @Test
    public void sendEvent() throws Exception {
        String eventName = "event_name";

        eventLogger.sendEvent(eventName);

        Event capturedEvent = captureEvent();
        assertThat(capturedEvent.getName(), equalTo(eventName));
        assertThat(capturedEvent.getPublicAddress(), equalTo(expectedPublicAddress));
        assertThat(capturedEvent.getType(), equalTo("event"));
        assertThat(capturedEvent.getFields().size(), equalTo(0));
    }

    private Event captureEvent() throws IOException {
        ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockAtnServer).sendEvent(eventArgumentCaptor.capture());
        return eventArgumentCaptor.getValue();
    }

    @Test
    public void startDurationLogging() throws Exception {
        String expectedEventName = "event_name";
        EventLogger.DurationLogger durationLogger = eventLogger.startDurationLogging();
        sleep(510);
        durationLogger.report(expectedEventName);

        Event capturedEvent = captureEvent();
        assertThat(capturedEvent.getName(), equalTo(expectedEventName));
        assertThat(capturedEvent.getPublicAddress(), equalTo(expectedPublicAddress));
        assertThat(capturedEvent.getType(), equalTo("event"));
        assertThat((Long) capturedEvent.getFields().get("duration"), greaterThan(500L));
        assertThat((Long) capturedEvent.getFields().get("duration"), lessThan(550L));
    }

    @Test
    public void sendErrorEvent() throws Exception {
        when(mockAndroidLogger.getPrintableStackTrace((Throwable) any())).thenReturn("java.lang.Exception: some error");
        String expectedEventName = "event_name";
        Exception expectedException = new Exception("some error");
        eventLogger.sendErrorEvent(expectedEventName, expectedException);

        Event capturedEvent = captureEvent();
        assertThat(capturedEvent.getName(), equalTo(expectedEventName));
        assertThat(capturedEvent.getPublicAddress(), equalTo(expectedPublicAddress));
        assertThat(capturedEvent.getType(), equalTo("error"));
        assertThat((String) capturedEvent.getFields().get("exception_type"), equalTo("Exception"));
        assertThat((String) capturedEvent.getFields().get("exception_msg"), equalTo("java.lang.Exception: some error"));
    }


    @Test
    public void sendEvent_Error_AndroidLog() throws Exception {
        doThrow(new IOException()).when(mockAtnServer).sendEvent((Event) any());

        eventLogger.sendEvent("event name");

        verify(mockAndroidLogger).log((Event) any());
        verify(mockAndroidLogger).log(eq("EventLogger - can't send event"));
    }

}