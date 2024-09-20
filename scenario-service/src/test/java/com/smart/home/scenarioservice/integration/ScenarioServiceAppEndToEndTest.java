package com.smart.home.scenarioservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.scenarioservice.client.AuthClient;
import com.smart.home.scenarioservice.client.DeviceTypeClient;
import com.smart.home.scenarioservice.handler.BadRequestException;
import com.smart.home.scenarioservice.model.Scenario;
import com.smart.home.scenarioservice.model.dto.DeviceTypeDTO;
import com.smart.home.scenarioservice.repository.ScenarioRepository;
import com.smart.home.scenarioservice.repository.model.ScenarioDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
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

@SpringBootTest
@ActiveProfiles("localhost")
@AutoConfigureMockMvc
public class ScenarioServiceAppEndToEndTest {

    private static final String URL = "/api/scenario";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScenarioRepository scenarioRepository;

    @MockBean
    private AuthClient authClient;

    @MockBean
    private DeviceTypeClient deviceTypeClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    private Scenario validScenario;
    private ScenarioDAO validScenarioDAO;
    private Scenario scenarioWithId;
    private ScenarioDAO scenarioDAOWithId;
    private Scenario invalidScenario;
    private DeviceTypeDTO deviceTypeDTO;
    private String token;

    @BeforeEach
    void clearCache(){
        Objects.requireNonNull(cacheManager.getCache("scenario")).clear();
    }

    @BeforeEach
    void setUp() {

        validScenario = new Scenario(
                null,
                1L,
                "temperature",
                "25",
                ">",
                "ON"
        );

        validScenarioDAO = new ScenarioDAO(
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

        scenarioDAOWithId = new ScenarioDAO(
                1L,
                1L,
                "description",
                "rain and snow",
                "=",
                "CLOSE"
        );

        invalidScenario = new Scenario();

        deviceTypeDTO = new DeviceTypeDTO();
        deviceTypeDTO.setId(1L);
        deviceTypeDTO.setName("Thermostat");

        token = "Bearer test-token";
    }

    @Test
    void testAddScenario_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString()))
                .thenReturn(deviceTypeDTO);
        when(scenarioRepository.save(any(ScenarioDAO.class))).thenReturn(scenarioDAOWithId);

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scenarioWithId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scenarioId").value(scenarioWithId.getScenarioId()))
                .andExpect(jsonPath("$.deviceTypeId").value(scenarioWithId.getDeviceTypeId()))
                .andExpect(jsonPath("$.weatherCondition").value(scenarioWithId.getWeatherCondition()))
                .andExpect(jsonPath("$.conditionValue").value(scenarioWithId.getConditionValue()))
                .andExpect(jsonPath("$.newStatus").value(scenarioWithId.getNewStatus()));


        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(1)).save(any(ScenarioDAO.class));
    }

    @Test
    void testAddScenario_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scenarioWithId)))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(0)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(0)).save(any(ScenarioDAO.class));
    }

    @Test
    void testAddScenario_NoToken() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scenarioWithId)))
                .andExpect(status().isForbidden());

        verify(authClient, times(0)).validateToken((anyString()));
        verify(deviceTypeClient, times(0)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(0)).save(any(ScenarioDAO.class));
    }

    @Test
    void testAddScenario_InvalidDeviceType() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString()))
                .thenThrow(BadRequestException.class);

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scenarioWithId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device Type validation failed for id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(0)).save(any(ScenarioDAO.class));
    }

    @Test
    void testAddScenario_InvalidData() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);

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

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(0)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(0)).save(any(ScenarioDAO.class));
    }

    @Test
    void testAddScenario_DataIntegrityViolation() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString())).thenReturn(deviceTypeDTO);
        when(scenarioRepository.save(any(ScenarioDAO.class))).thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        mockMvc.perform(post(URL)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScenario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid data or foreign key constraint violation"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(1)).save(any(ScenarioDAO.class));
    }

    @Test
    void testUpdateScenario_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString()))
                .thenReturn(deviceTypeDTO);
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.of(validScenarioDAO));
        when(scenarioRepository.save(any(ScenarioDAO.class))).thenReturn(validScenarioDAO);

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

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(1)).findById(anyLong());
        verify(scenarioRepository, times(1)).save(any(ScenarioDAO.class));
    }

    @Test
    void testUpdateScenario_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScenario)))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(0)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(0)).findById(anyLong());
        verify(scenarioRepository, times(0)).save(any(ScenarioDAO.class));
    }

    @Test
    void testUpdateScenario_InvalidDeviceType() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString()))
                .thenThrow(BadRequestException.class);

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScenario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Device Type validation failed for id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(0)).findById(anyLong());
        verify(scenarioRepository, times(0)).save(any(ScenarioDAO.class));
    }

    @Test
    void testUpdateScenario_NotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString())).thenReturn(deviceTypeDTO);
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScenario)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Scenario not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(1)).findById(anyLong());
        verify(scenarioRepository, times(0)).save(any(ScenarioDAO.class));
    }

    @Test
    void testUpdateScenario_InvalidData() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);

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

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(0)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(0)).findById(anyLong());
        verify(scenarioRepository, times(0)).save(any(ScenarioDAO.class));
    }

    @Test
    void testUpdateScenario_UnexpectedError() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceTypeClient.getDeviceTypeById(anyLong(), anyString())).thenReturn(deviceTypeDTO);
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.of(validScenarioDAO));
        when(scenarioRepository.save(any(ScenarioDAO.class))).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(put(URL + "/{id}", 1L)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validScenario)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").value("An unexpected error occurred. Please try again later."));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(deviceTypeClient, times(1)).getDeviceTypeById(anyLong(), anyString());
        verify(scenarioRepository, times(1)).findById(anyLong());
        verify(scenarioRepository, times(1)).save(any(ScenarioDAO.class));
    }

    @Test
    void testDeleteScenarioById_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.of(validScenarioDAO));
        doNothing().when(scenarioRepository).delete(any(ScenarioDAO.class));

        mockMvc.perform(delete(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(1)).findById(anyLong());
        verify(scenarioRepository, times(1)).delete(any(ScenarioDAO.class));
    }

    @Test
    void testDeleteScenarioById_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(delete(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(0)).findById(anyLong());
        verify(scenarioRepository, times(0)).delete(any(ScenarioDAO.class));
    }

    @Test
    void testDeleteScenarioById_NotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(delete(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Scenario not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(1)).findById(anyLong());
        verify(scenarioRepository, times(0)).delete(any(ScenarioDAO.class));
    }

    @Test
    void testGetScenarioById_Success() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.of(scenarioDAOWithId));

        mockMvc.perform(get(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.scenarioId").value(1L))
                .andExpect(jsonPath("$.deviceTypeId").value(1L))
                .andExpect(jsonPath("$.weatherCondition").value("description"))
                .andExpect(jsonPath("$.conditionValue").value("rain and snow"))
                .andExpect(jsonPath("$.operator").value("="))
                .andExpect(jsonPath("$.newStatus").value("CLOSE"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetScenarioById_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(0)).findById(anyLong());
    }

    @Test
    void testGetScenarioById_NotFound() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(scenarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get(URL + "/{id}", 1L)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Scenario not found with id: 1"));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(1)).findById(anyLong());
    }

    @Test
    void testGetAllScenarios_Success() throws Exception {
        List<ScenarioDAO> scenarioDAOList = Arrays.asList(scenarioDAOWithId, validScenarioDAO);
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(scenarioRepository.findAll()).thenReturn(scenarioDAOList);

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
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

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(1)).findAll();
    }

    @Test
    void testGetAllScenarios_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(0)).findAll();
    }

    @Test
    void testGetAllScenarios_EmptyList() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(scenarioRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(1)).findAll();
    }

    @Test
    void testGetScenariosByDeviceTypeId_Success() throws Exception {
        List<ScenarioDAO> scenarioDAOList = Arrays.asList(scenarioDAOWithId, validScenarioDAO);
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(scenarioRepository.findByDeviceTypeId(anyLong())).thenReturn(scenarioDAOList);

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

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(1)).findByDeviceTypeId(anyLong());
    }

    @Test
    void testGetScenariosByDeviceTypeId_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get(URL + "/device")
                        .header("Authorization", token)
                        .param("deviceTypeId", "2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(0)).findByDeviceTypeId(anyLong());
    }

    @Test
    void testGetScenariosByDeviceTypeId_EmptyList() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);
        when(scenarioRepository.findByDeviceTypeId(anyLong())).thenReturn(Collections.emptyList());

        mockMvc.perform(get(URL + "/device")
                        .header("Authorization", token)
                        .param("deviceTypeId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(1)).findByDeviceTypeId(anyLong());
    }

    @Test
    void testGetScenariosByDeviceTypeId_InvalidId() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(true);

        mockMvc.perform(get(URL + "/device")
                        .header("Authorization", token)
                        .param("deviceTypeId", "invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid parameter type. Please provide a valid value."));

        verify(authClient, times(1)).validateToken((anyString()));
        verify(scenarioRepository, times(0)).findByDeviceTypeId(anyLong());
    }
}
