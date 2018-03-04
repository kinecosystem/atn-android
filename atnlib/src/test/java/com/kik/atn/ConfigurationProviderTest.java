package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static java.lang.Thread.sleep;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ConfigurationProviderTest {

    private static final String PUBLIC_KEY = "GDYF6ZDSSLM32OKGOL6ZKA4JYSBFSHLSARUUPE4YDYNOHJ5WXSLMBDUV";

    @Mock
    private ATNServer mockAtnServer;
    @Mock
    private EventLogger mockEventLogger;
    private ConfigurationProvider configurationProvider;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        configurationProvider = new ConfigurationProvider(mockAtnServer, mockEventLogger, 1000);
    }

    @Test
    public void getConfig_Success() throws Exception {
        String expectedAtnAddress = "GDHPNRNU5PCP46DPW3MHK74XQU3BOMVBJKCTW3DV4WZNBOJB6JTJTFXC";
        when(mockAtnServer.getConfiguration(PUBLIC_KEY))
                .thenReturn(new Config(true, expectedAtnAddress));

        Config config = configurationProvider.getConfig(PUBLIC_KEY);
        assertThat(config.isEnabled(), equalTo(true));
        assertThat(config.getAtnAddress(), equalTo(expectedAtnAddress));
        verifyZeroInteractions(mockEventLogger);
    }

    @Test
    public void getConfig_Failure_Report() throws Exception {
        IOException expectedException = new IOException("some error");
        when(mockAtnServer.getConfiguration(PUBLIC_KEY))
                .thenThrow(expectedException);

        Config config = configurationProvider.getConfig(PUBLIC_KEY);
        assertThat(config.isEnabled(), equalTo(false));
        assertThat(config.getAtnAddress(), nullValue());
        verify(mockEventLogger).sendErrorEvent("get_config_failed", expectedException);
    }

    @Test
    public void getConfig_2InARowBelowUpdateInterval_SecondGetIsCached() throws Exception {
        String expectedAtnAddress = "GDHPNRNU5PCP46DPW3MHK74XQU3BOMVBJKCTW3DV4WZNBOJB6JTJTFXC";
        when(mockAtnServer.getConfiguration(PUBLIC_KEY))
                .thenReturn(new Config(true, expectedAtnAddress));

        Config config = configurationProvider.getConfig(PUBLIC_KEY);
        assertThat(config.isEnabled(), equalTo(true));
        assertThat(config.getAtnAddress(), equalTo(expectedAtnAddress));
        config = configurationProvider.getConfig(PUBLIC_KEY);
        assertThat(config.isEnabled(), equalTo(true));
        assertThat(config.getAtnAddress(), equalTo(expectedAtnAddress));

        verify(mockAtnServer, times(1)).getConfiguration(anyString());
        verifyZeroInteractions(mockEventLogger);
    }

    @Test
    public void getConfig_2InARowAboveUpdateInterval_2CallsToServer() throws Exception {
        configurationProvider = new ConfigurationProvider(mockAtnServer, mockEventLogger, 100);

        String expectedAtnAddress = "GDHPNRNU5PCP46DPW3MHK74XQU3BOMVBJKCTW3DV4WZNBOJB6JTJTFXC";
        when(mockAtnServer.getConfiguration(PUBLIC_KEY))
                .thenReturn(new Config(true, expectedAtnAddress));

        Config config = configurationProvider.getConfig(PUBLIC_KEY);
        assertThat(config.isEnabled(), equalTo(true));
        assertThat(config.getAtnAddress(), equalTo(expectedAtnAddress));
        sleep(150);
        when(mockAtnServer.getConfiguration(PUBLIC_KEY))
                .thenReturn(new Config(false, "GBGOKR54OQ7GM7RG5R43ENRH4PCPIFQ4QPJQ5EDJJCSRO2JSNXNLUNNH"));
        config = configurationProvider.getConfig(PUBLIC_KEY);
        assertThat(config.isEnabled(), equalTo(false));
        assertThat(config.getAtnAddress(), equalTo("GBGOKR54OQ7GM7RG5R43ENRH4PCPIFQ4QPJQ5EDJJCSRO2JSNXNLUNNH"));

        verify(mockAtnServer, times(2)).getConfiguration(anyString());
        verifyZeroInteractions(mockEventLogger);
    }
}