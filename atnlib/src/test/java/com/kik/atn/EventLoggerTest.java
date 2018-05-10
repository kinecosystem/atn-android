package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@org.robolectric.annotation.Config(sdk = 23, manifest = org.robolectric.annotation.Config.NONE)
public class EventLoggerTest {

    private static final String DEVICE_ID = "some_random_string";
    private static final String ORBS_PUBLIC_ADDRESS = "e2ab34568edf08adb91ade7e535300aac11655ef21c9fa54712791983964aa6f";
    private static final String PUBLIC_ADDRESS = "GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV";
    @Mock
    private ATNServer mockAtnServer;
    @Mock
    private AndroidLogger mockAndroidLogger;
    @Mock
    private LocalStore mockLocalStore;
    private EventLogger eventLogger;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(mockLocalStore.getString(anyString())).thenReturn(DEVICE_ID);
        eventLogger = new EventLogger(mockAtnServer, mockAndroidLogger, mockLocalStore, false);

        eventLogger.setPublicAddress(PUBLIC_ADDRESS);
        eventLogger.setOrbsPublicAddress(ORBS_PUBLIC_ADDRESS);
    }

    @Test
    public void sendEvent() throws Exception {
        String eventName = "event_name";

        eventLogger.sendEvent(eventName);

        Event capturedEvent = captureEvent();
        assertThat(capturedEvent.getName(), equalTo(eventName));
        assertThat(capturedEvent.getPublicAddress(), equalTo(PUBLIC_ADDRESS));
        assertThat(capturedEvent.getType(), equalTo("event"));
        assertThat(capturedEvent.getLibraryVersion(), equalTo(BuildConfig.VERSION_NAME));
        assertThat(capturedEvent.getBlockchain(), equalTo(Event.BLOCKCHAIN_KIN));
        assertThat(capturedEvent.getDeviceId(), equalTo(DEVICE_ID));
        Map<String, Object> fields = capturedEvent.getFields();
        assertThat(fields.size(), equalTo(3));
    }

    @Test
    public void sendOrbsEvent() throws Exception {
        String eventName = "event_name";

        eventLogger.sendOrbsEvent(eventName);

        Event capturedEvent = captureEvent();
        assertThat(capturedEvent.getName(), equalTo(eventName));
        assertThat(capturedEvent.getPublicAddress(), equalTo(ORBS_PUBLIC_ADDRESS));
        assertThat(capturedEvent.getType(), equalTo("event"));
        assertThat(capturedEvent.getLibraryVersion(), equalTo(BuildConfig.VERSION_NAME));
        assertThat(capturedEvent.getBlockchain(), equalTo(Event.BLOCKCHAIN_ORBS));
        assertThat(capturedEvent.getDeviceId(), equalTo(DEVICE_ID));
        Map<String, Object> fields = capturedEvent.getFields();
        assertThat(fields.size(), equalTo(3));
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
        assertThat(capturedEvent.getPublicAddress(), equalTo(PUBLIC_ADDRESS));
        assertThat(capturedEvent.getType(), equalTo("event"));
        assertThat(capturedEvent.getLibraryVersion(), equalTo(BuildConfig.VERSION_NAME));
        assertThat(capturedEvent.getBlockchain(), equalTo(Event.BLOCKCHAIN_KIN));
        assertThat(capturedEvent.getDeviceId(), equalTo(DEVICE_ID));
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
        assertThat(capturedEvent.getPublicAddress(), equalTo(PUBLIC_ADDRESS));
        assertThat(capturedEvent.getType(), equalTo("error"));
        assertThat(capturedEvent.getLibraryVersion(), equalTo(BuildConfig.VERSION_NAME));
        assertThat(capturedEvent.getBlockchain(), equalTo(Event.BLOCKCHAIN_KIN));
        assertThat(capturedEvent.getDeviceId(), equalTo(DEVICE_ID));
        assertThat((String) capturedEvent.getFields().get("exception_type"), equalTo("Exception"));
        assertThat((String) capturedEvent.getFields().get("exception_msg"), equalTo("java.lang.Exception: some error"));
    }

    @Test
    public void sendOrbsErrorEvent() throws Exception {
        when(mockAndroidLogger.getPrintableStackTrace((Throwable) any())).thenReturn("java.lang.Exception: some error");
        String expectedEventName = "event_name";
        Exception expectedException = new Exception("some error");
        eventLogger.sendOrbsErrorEvent(expectedEventName, expectedException);

        Event capturedEvent = captureEvent();
        assertThat(capturedEvent.getName(), equalTo(expectedEventName));
        assertThat(capturedEvent.getPublicAddress(), equalTo(ORBS_PUBLIC_ADDRESS));
        assertThat(capturedEvent.getType(), equalTo("error"));
        assertThat(capturedEvent.getLibraryVersion(), equalTo(BuildConfig.VERSION_NAME));
        assertThat(capturedEvent.getBlockchain(), equalTo(Event.BLOCKCHAIN_ORBS));
        assertThat(capturedEvent.getDeviceId(), equalTo(DEVICE_ID));
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