package com.smart.home.deviceservice.controller;

import com.smart.home.deviceservice.handler.GlobalExceptionHandler;
import com.smart.home.deviceservice.service.ScenarioEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScenarioEvaluationControllerTest {

    private static final String URL = "/api/scenario-evaluation/evaluate";

    @Mock
    private ScenarioEvaluationService scenarioEvaluationService;

    @InjectMocks
    private ScenarioEvaluationController scenarioEvaluationController;

    private MockMvc mockMvc;

    private String token;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(scenarioEvaluationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        token = "Bearer token";
    }

    @Test
    void testEvaluateScenariosAndPerformActions_Success() throws Exception {
        doNothing().when(scenarioEvaluationService).evaluateAndPerformDeviceStatusChange(anyString());

        mockMvc.perform(get(URL)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Scenario evaluation and device status changes performed successfully."));

        verify(scenarioEvaluationService, times(1)).evaluateAndPerformDeviceStatusChange(token);
    }

    @Test
    void testEvaluateScenariosAndPerformActions_Failure() throws Exception {
        doThrow(new RuntimeException("Scenario evaluation failed")).when(scenarioEvaluationService).evaluateAndPerformDeviceStatusChange(anyString());

        mockMvc.perform(get(URL)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred. Please try again later."));

        verify(scenarioEvaluationService, times(1)).evaluateAndPerformDeviceStatusChange(token);
    }

}