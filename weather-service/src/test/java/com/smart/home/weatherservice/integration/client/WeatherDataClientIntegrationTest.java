package com.smart.home.weatherservice.integration.client;

import com.smart.home.weatherservice.client.WeatherDataClient;
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

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("localhost")
public class WeatherDataClientIntegrationTest {

    @Autowired
    private WeatherDataClient weatherDataClient;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        ReflectionTestUtils.setField(weatherDataClient, "weatherApiUrl", mockWebServer.url("/").toString());

        ReflectionTestUtils.setField(weatherDataClient, "apiKey", "test-api-key");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getWeatherData_Success() throws Exception {
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"temp\":25,\"humidity\":60}")
                .addHeader("Content-Type", "application/json"));

        String response = weatherDataClient.getWeatherData(51.5074, -0.1278);

        assertEquals("{\"temp\":25,\"humidity\":60}", response);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/?lat=51.5074&lon=-0.1278&appid=test-api-key&units=metric", request.getPath());
    }

    @Test
    void getWeatherData_Failure() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        String response = weatherDataClient.getWeatherData(51.5074, -0.1278);

        assertNull(response);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/?lat=51.5074&lon=-0.1278&appid=test-api-key&units=metric", request.getPath());
    }

}
