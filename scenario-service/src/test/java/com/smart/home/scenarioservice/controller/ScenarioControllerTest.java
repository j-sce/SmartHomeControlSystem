package com.smart.home.scenarioservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.scenarioservice.handler.BadRequestException;
import com.smart.home.scenarioservice.handler.GlobalExceptionHandler;
import com.smart.home.scenarioservice.model.Scenario;
import com.smart.home.scenarioservice.service.ScenarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScenarioControllerTest {

    private static final String URL = "/api/scenario";

    @Mock
    private ScenarioService scenarioService;

    @InjectMocks
    private ScenarioController scenarioController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Scenario validScenario;
    private Scenario scenarioWithId;
    private Scenario invalidScenario;
    private String token;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(scenarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void initializeObjects() {
        validScenario = new Scenario(
                null,
                1L,
                "temperature",
                "25",
                ">",
                "ON"
        );

        scenarioWithId = new Scenario(
                1L,
                1L,
                "description",
                "rain and snow",
                "=",
                "CLOSE"
        );

        invalidScenario = new Scenario();

        token = "Bearer token";
    }

    @Test
    void testGetAllScenarios() throws Exception {
        List<Scenario> scenarioList = Arrays.asList(scenarioWithId, validScenario);
        when(scenarioService.getAllScenarios()).thenReturn(scenarioList);

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].scenarioId").value(scenarioWithId.getScenarioId()))
                .andExpect(jsonPath("$[0].deviceTypeId").value(scenarioWithId.getDeviceTypeId()))
                .andExpect(jsonPath("$[0].weatherCondition").value(scenarioWithId.getWeatherCondition()))
                .andExpect(jsonPath("$[0].conditionValue").value(scenarioWithId.getConditionValue()))
                .andExpect(jsonPath("$[0].newStatus").value(scenarioWithId.getNewStatus()))
                .andExpect(jsonPath("$[1].scenarioId").doesNotExist())
                .andExpect(jsonPath("$[1].deviceTypeId").value(validScenario.getDeviceTypeId()))
                .andExpect(jsonPath("$[1].weatherCondition").value(validScenario.getWeatherCondition()))
                .andExpect(jsonPath("$[1].conditionValue").value(validScenario.getConditionValue()))
                .andExpect(jsonPath("$[1].newStatus").value(validScenario.getNewStatus()));

        verify(scenarioService, times(1)).getAllScenarios();
    }

    @Test
    void testGetAllScenarios_EmptyList() throws Exception {
        when(scenarioService.getAllScenarios()).thenReturn(List.of());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(scenarioService, times(1)).getAllScenarios();
    }

    @Test
    void testGetScenarioById_Success() throws Exception {
        when(scenarioService.getScenarioById(1L)).thenReturn(scenarioWithId);

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioId").value(1L))
                .andExpect(jsonPath("$.deviceTypeId").value(1L))
                .andExpect(jsonPath("$.weatherCondition").value("description"))
                .andExpect(jsonPath("$.conditionValue").value("rain and snow"))
                .andExpect(jsonPath("$.operator").value("="))
                .andExpect(jsonPath("$.newStatus").value("CLOSE"));

        verify(scenarioService, times(1)).getScenarioById(1L);
    }

    @Test
    void testGetScenarioById_NotFound() throws Exception {
        when(scenarioService.getScenarioById(1L)).thenThrow(new BadRequestException("Scenario not found with id: 1"));

        mockMvc.perform(get(URL + "/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Scenario not found with id: 1"));

        verify(scenarioService, times(1)).getScenarioById(1L);
    }

    @Test
    void testAddScenario_Success() throws Exception {
        when(scenarioService.addScenario(any(Scenario.class), anyString()))
                .thenReturn(scenarioWithId);

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScenario)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioId").value(scenarioWithId.getScenarioId()))
                .andExpect(jsonPath("$.deviceTypeId").value(scenarioWithId.getDeviceTypeId()))
                .andExpect(jsonPath("$.weatherCondition").value(scenarioWithId.getWeatherCondition()))
                .andExpect(jsonPath("$.conditionValue").value(scenarioWithId.getConditionValue()))
                .andExpect(jsonPath("$.newStatus").value(scenarioWithId.getNewStatus()));

        verify(scenarioService).addScenario(any(Scenario.class), anyString());
    }

    @Test
    void testAddScenario_InvalidData() throws Exception {

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidScenario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        "deviceTypeId: Device type id must not be null",
                        "weatherCondition: Weather condition must not be blank",
                        "conditionValue: Weather condition value must not be blank",
                        "operator: Weather condition value must not be blank",
                        "newStatus: Status must not be blank")));

        verify(scenarioService, never()).addScenario(any(Scenario.class), anyString());
    }

    @Test
    void testUpdateScenarioById_Success() throws Exception {
        when(scenarioService.updateScenarioById(eq(1L), any(Scenario.class), anyString()))
                .thenReturn(validScenario);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScenario)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deviceTypeId").value(validScenario.getDeviceTypeId()))
                .andExpect(jsonPath("$.weatherCondition").value(validScenario.getWeatherCondition()))
                .andExpect(jsonPath("$.conditionValue").value(validScenario.getConditionValue()))
                .andExpect(jsonPath("$.newStatus").value(validScenario.getNewStatus()));

        verify(scenarioService).updateScenarioById(eq(1L), any(Scenario.class), anyString());
    }

    @Test
    void testUpdateScenarioById_InvalidData() throws Exception {

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidScenario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        "deviceTypeId: Device type id must not be null",
                        "weatherCondition: Weather condition must not be blank",
                        "conditionValue: Weather condition value must not be blank",
                        "operator: Weather condition value must not be blank",
                        "newStatus: Status must not be blank")));

        verify(scenarioService, times(0)).updateScenarioById(anyLong(), any(Scenario.class), anyString());
    }

    @Test
    void testDeleteScenarioById_Success() throws Exception {
        doNothing().when(scenarioService).deleteScenarioById(1L);

        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(scenarioService, times(1)).deleteScenarioById(1L);
    }

    @Test
    void testDeleteScenarioById_NotFound() throws Exception {
        doThrow(new BadRequestException("Scenario not found with id: 1")).when(scenarioService).deleteScenarioById(1L);

        mockMvc.perform(delete(URL + "/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Scenario not found with id: 1"));

        verify(scenarioService, times(1)).deleteScenarioById(1L);
    }

    @Test
    void testGetScenariosByDeviceTypeId_Success() throws Exception {
        List<Scenario> scenarioList = Arrays.asList(scenarioWithId, validScenario);
        when(scenarioService.getScenariosByDeviceTypeId(anyLong(), anyString())).thenReturn(scenarioList);

        mockMvc.perform(get(URL + "/device")
                        .header("Authorization", token)
                        .param("deviceTypeId", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].scenarioId").value(scenarioWithId.getScenarioId()))
                .andExpect(jsonPath("$[0].deviceTypeId").value(scenarioWithId.getDeviceTypeId()))
                .andExpect(jsonPath("$[0].weatherCondition").value(scenarioWithId.getWeatherCondition()))
                .andExpect(jsonPath("$[0].conditionValue").value(scenarioWithId.getConditionValue()))
                .andExpect(jsonPath("$[0].newStatus").value(scenarioWithId.getNewStatus()))
                .andExpect(jsonPath("$[1].scenarioId").doesNotExist())
                .andExpect(jsonPath("$[1].deviceTypeId").value(validScenario.getDeviceTypeId()))
                .andExpect(jsonPath("$[1].weatherCondition").value(validScenario.getWeatherCondition()))
                .andExpect(jsonPath("$[1].conditionValue").value(validScenario.getConditionValue()))
                .andExpect(jsonPath("$[1].newStatus").value(validScenario.getNewStatus()));

        verify(scenarioService, times(1)).getScenariosByDeviceTypeId(anyLong(), anyString());
    }

    @Test
    void testGetScenariosByDeviceTypeId_EmptyList() throws Exception {
        when(scenarioService.getScenariosByDeviceTypeId(anyLong(), anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL + "/device")
                        .header("Authorization", token)
                        .param("deviceTypeId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(scenarioService, times(1)).getScenariosByDeviceTypeId(anyLong(), anyString());
    }

    @Test
    void testGetScenariosByDeviceTypeId_InvalidId() throws Exception {
        mockMvc.perform(get(URL + "/device")
                        .header("Authorization", token)
                        .param("deviceTypeId", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(scenarioService, never()).getScenariosByDeviceTypeId(anyLong(), anyString());
    }

}
