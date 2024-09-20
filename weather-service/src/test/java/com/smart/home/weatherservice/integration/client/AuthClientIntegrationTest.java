package com.smart.home.weatherservice.integration.client;

import com.smart.home.weatherservice.client.AuthClient;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("localhost")
public class AuthClientIntegrationTest {

    @Autowired
    private AuthClient authClient;

    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        ReflectionTestUtils.setField(authClient, "authApiUrl", mockWebServer.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void validateToken_Success() throws Exception {
        String mockResponse = "true";
        mockWebServer.enqueue(new MockResponse().setBody(mockResponse).addHeader("Content-Type", "application/json"));

        boolean isValid = authClient.validateToken("Bearer test-token");

        assertTrue(isValid);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/", request.getPath());
    }

    @Test
    void validateToken_Failure() throws Exception {
        mockWebServer.enqueue(new MockResponse().setResponseCode(403));

        boolean isValid = authClient.validateToken("Bearer invalidToken");

        assertFalse(isValid);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/", request.getPath());
    }

}
