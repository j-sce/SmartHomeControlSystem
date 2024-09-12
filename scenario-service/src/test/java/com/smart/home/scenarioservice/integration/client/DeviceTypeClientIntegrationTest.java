package com.smart.home.scenarioservice.integration.client;

import com.smart.home.scenarioservice.client.DeviceTypeClient;
import com.smart.home.scenarioservice.model.dto.DeviceTypeDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeviceTypeClientIntegrationTest {

    @Autowired
    private DeviceTypeClient deviceTypeClient;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        ReflectionTestUtils.setField(deviceTypeClient, "deviceTypeApiUrl", mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getDeviceTypeById_Success() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":1,\"name\":\"DeviceType1\"}")
                .addHeader("Content-Type", "application/json"));

        DeviceTypeDTO response = deviceTypeClient.getDeviceTypeById(1L, "Bearer test-token");

        assertEquals(1L, response.getId());
        assertEquals("DeviceType1", response.getName());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/1", request.getPath());
        assertEquals("Bearer test-token", request.getHeader("Authorization"));
    }

    @Test
    void getDeviceTypeById_Failure() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(WebClientResponseException.class, () -> {
            deviceTypeClient.getDeviceTypeById(1L, "Bearer invalidToken");
        });

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/1", request.getPath());
        assertEquals("Bearer invalidToken", request.getHeader("Authorization"));
    }

}
