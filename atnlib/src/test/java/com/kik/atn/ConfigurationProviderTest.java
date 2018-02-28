package com.kik.atn;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
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
        configurationProvider = new ConfigurationProvider(mockAtnServer, mockEventLogger);
    }

    @Test
    public void init_Success() throws Exception {
        String expectedAtnAddress = "GDHPNRNU5PCP46DPW3MHK74XQU3BOMVBJKCTW3DV4WZNBOJB6JTJTFXC";
        when(mockAtnServer.getConfiguration(PUBLIC_KEY))
                .thenReturn(new Config(true, expectedAtnAddress));

        configurationProvider.init(PUBLIC_KEY);
        assertThat(configurationProvider.enabled(), equalTo(true));
        assertThat(configurationProvider.ATNAddress(), equalTo(expectedAtnAddress));
        verifyZeroInteractions(mockEventLogger);
    }

    @Test
    public void init_Failure_Report() throws Exception {
        IOException expectedException = new IOException("some error");
        when(mockAtnServer.getConfiguration(PUBLIC_KEY))
                .thenThrow(expectedException);

        configurationProvider.init(PUBLIC_KEY);
        assertThat(configurationProvider.enabled(), equalTo(false));
        assertThat(configurationProvider.ATNAddress(), nullValue());
        verify(mockEventLogger).sendErrorEvent("get_config_failed", expectedException);
    }

}