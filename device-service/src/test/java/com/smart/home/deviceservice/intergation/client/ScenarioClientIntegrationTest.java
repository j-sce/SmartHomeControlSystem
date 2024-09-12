package com.smart.home.deviceservice.intergation.client;

import com.smart.home.deviceservice.client.ScenarioClient;
import com.smart.home.deviceservice.model.dto.ScenarioDTO;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScenarioClientIntegrationTest {

    @Autowired
    private ScenarioClient scenarioClient;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ReflectionTestUtils.setField(scenarioClient, "scenarioApiUrl", mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getScenariosByDeviceTypeId_Success() throws Exception {
        String mockResponse = "[{\"scenarioId\": 1, \"deviceType\": 1, \"newStatus\": \"ON\"}]";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).addHeader("Content-Type", "application/json"));

        List<ScenarioDTO> scenarios = scenarioClient.getScenariosByDeviceTypeId(1L, "Bearer test-token");

        assertEquals(1, scenarios.size());
        assertEquals("ON", scenarios.getFirst().getNewStatus());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/?deviceTypeId=1", request.getPath());
        assertEquals("Bearer test-token", request.getHeader("Authorization"));
    }

    @Test
    void getScenariosByDeviceTypeId_Failure() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        assertThrows(WebClientResponseException.class, () -> {
            List<ScenarioDTO> scenariosByDeviceTypeId = scenarioClient.getScenariosByDeviceTypeId(1L, "Bearer invalidToken");
        });

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/?deviceTypeId=1", request.getPath());
        assertEquals("Bearer invalidToken", request.getHeader("Authorization"));
    }
}
