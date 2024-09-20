package com.smart.home.deviceservice.integration.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.deviceservice.client.WeatherDataClient;
import com.smart.home.deviceservice.model.dto.WeatherDataDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("localhost")
public class WeatherDataClientIntegrationTest {

    @Autowired
    private WeatherDataClient weatherDataClient;

    private MockWebServer mockWebServer;

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ReflectionTestUtils.setField(weatherDataClient, "weatherDataApiUrl", mockWebServer.url("/").toString());
    }

    @AfterEach
    public void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetWeatherData_Success() throws Exception {
        WeatherDataDTO mockResponse = new WeatherDataDTO();
        mockResponse.setTemperature(25.0);
        mockResponse.setHumidity(60);

        mockWebServer.enqueue(new MockResponse()
                .setBody(new ObjectMapper().writeValueAsString(mockResponse))
                .addHeader("Content-Type", "application/json"));

        WeatherDataDTO response = weatherDataClient.getWeatherData(40.7128, -74.0068, "Bearer token");

        assertNotNull(response);
        assertEquals(25.0, response.getTemperature());
        assertEquals(60, response.getHumidity());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/?lat=40.7128&lon=-74.0068", request.getPath());
        assertEquals("Bearer token", request.getHeader("Authorization"));
    }

    @Test
    void getWeatherData_Failure() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(WebClientResponseException.class, () -> {
            weatherDataClient.getWeatherData(52.52, 13.405, "Bearer invalidToken");
        });

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/?lat=52.52&lon=13.405", request.getPath());
        assertEquals("Bearer invalidToken", request.getHeader("Authorization"));
    }
}
