package com.smart.home.deviceservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.deviceservice.client.AuthClient;
import com.smart.home.deviceservice.client.ScenarioClient;
import com.smart.home.deviceservice.client.WeatherDataClient;
import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.model.dto.ScenarioDTO;
import com.smart.home.deviceservice.model.dto.WeatherDataDTO;
import com.smart.home.deviceservice.repository.DeviceRepository;
import com.smart.home.deviceservice.repository.DeviceStatusChangeRepository;
import com.smart.home.deviceservice.repository.model.DeviceDAO;
import com.smart.home.deviceservice.repository.model.DeviceStatusChangeDAO;
import com.smart.home.deviceservice.repository.model.DeviceTypeDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("localhost")
@AutoConfigureMockMvc
public class ScenarioEvaluationServiceEndToEndTest {

    private static final String URL = "/api/scenario-evaluation/evaluate";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceRepository deviceRepository;

    @MockBean
    private DeviceStatusChangeRepository deviceStatusChangeRepository;

    @MockBean
    private AuthClient authClient;

    @MockBean
    private ScenarioClient scenarioClient;

    @MockBean
    private WeatherDataClient weatherDataClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    private String token;

    @BeforeEach
    void setUp() {
        Objects.requireNonNull(cacheManager.getCache("device")).clear();
        Objects.requireNonNull(cacheManager.getCache("deviceType")).clear();
        token = "Bearer test-token";
    }

    @Test
    void testEvaluateScenariosAndPerformActions_Success() throws Exception {
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setScenarioId(200L);
        scenario.setWeatherCondition("temperature");
        scenario.setDeviceTypeId(101L);
        scenario.setOperator(">");
        scenario.setConditionValue("20");
        scenario.setNewStatus("ON");

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setDeviceId(1L);
        deviceDAO.setDeviceType(new DeviceTypeDAO(101L, "Thermostat"));
        deviceDAO.setStatus("OFF");
        deviceDAO.setLatitude(40.7128);
        deviceDAO.setLongitude(-74.0060);

        DeviceDAO updatedDeviceDAO = new DeviceDAO();
        updatedDeviceDAO.setDeviceId(1L);
        updatedDeviceDAO.setDeviceType(new DeviceTypeDAO(101L, "Thermostat"));
        updatedDeviceDAO.setStatus("ON");
        updatedDeviceDAO.setLatitude(40.7128);
        updatedDeviceDAO.setLongitude(-74.0060);

        DeviceStatusChangeDAO deviceStatusChangeDAO = new DeviceStatusChangeDAO();
        deviceStatusChangeDAO.setDevice(deviceDAO);
        deviceStatusChangeDAO.setOldStatus("OFF");
        deviceStatusChangeDAO.setNewStatus("ON");
        deviceStatusChangeDAO.setWeatherCondition("temperature");
        deviceStatusChangeDAO.setScenarioId(200L);

        WeatherDataDTO weatherData = new WeatherDataDTO();
        weatherData.setTemperature(25.0);

        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findAll()).thenReturn(List.of(deviceDAO));
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(deviceDAO));
        when(scenarioClient.getScenariosByDeviceTypeId(anyLong(), anyString())).thenReturn(List.of(scenario));
        when(weatherDataClient.getWeatherData(anyDouble(), anyDouble(), anyString())).thenReturn(weatherData);
        when(deviceRepository.save(any(DeviceDAO.class))).thenReturn(updatedDeviceDAO);
        when(deviceStatusChangeRepository.save(any(DeviceStatusChangeDAO.class))).thenReturn(deviceStatusChangeDAO);

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Scenario evaluation and device status changes performed successfully."));

        verify(authClient, times(1)).validateToken(anyString());
        verify(scenarioClient, times(1)).getScenariosByDeviceTypeId(anyLong(), anyString());
        verify(weatherDataClient, times(1)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(deviceRepository, times(1)).findAll();
        verify(deviceRepository, times(2)).findById(anyLong());
        verify(deviceRepository, times(1)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(1)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testEvaluateScenariosAndPerformActionsWithSeveralDevicesAndScenarios_Success() throws Exception {
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setScenarioId(1L);
        scenario.setDeviceTypeId(1L);
        scenario.setWeatherCondition("humidity");
        scenario.setOperator(">");
        scenario.setConditionValue("80");
        scenario.setNewStatus("ON");

        ScenarioDTO scenario2 = new ScenarioDTO();
        scenario2.setScenarioId(1L);
        scenario2.setDeviceTypeId(2L);
        scenario2.setWeatherCondition("weather id");
        scenario2.setOperator("=");
        scenario2.setConditionValue("800");
        scenario2.setNewStatus("OPEN");

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setDeviceId(1L);
        deviceDAO.setDeviceType(new DeviceTypeDAO(1L, "Thermostat"));
        deviceDAO.setStatus("OFF");
        deviceDAO.setLatitude(40.7128);
        deviceDAO.setLongitude(-74.0060);

        DeviceDAO deviceDAO2 = new DeviceDAO();
        deviceDAO2.setDeviceId(2L);
        deviceDAO2.setDeviceType(new DeviceTypeDAO(2L, "Smart Light"));
        deviceDAO2.setStatus("OFF");
        deviceDAO2.setLatitude(54.9097);
        deviceDAO2.setLongitude(80.0055);

        DeviceDAO updatedDeviceDAO = new DeviceDAO();
        updatedDeviceDAO.setDeviceId(1L);
        updatedDeviceDAO.setDeviceType(new DeviceTypeDAO(1L, "Thermostat"));
        updatedDeviceDAO.setStatus("ON");
        updatedDeviceDAO.setLatitude(40.7128);
        updatedDeviceDAO.setLongitude(-74.0060);

        DeviceDAO updatedDeviceDAO2 = new DeviceDAO();
        updatedDeviceDAO2.setDeviceId(2L);
        updatedDeviceDAO2.setDeviceType(new DeviceTypeDAO(1L, "Smart Light"));
        updatedDeviceDAO2.setStatus("OPEN");
        updatedDeviceDAO2.setLatitude(54.9097);
        updatedDeviceDAO2.setLongitude(80.0055);

        DeviceStatusChangeDAO deviceStatusChangeDAO = new DeviceStatusChangeDAO();
        deviceStatusChangeDAO.setDevice(deviceDAO);
        deviceStatusChangeDAO.setOldStatus("OFF");
        deviceStatusChangeDAO.setNewStatus("ON");
        deviceStatusChangeDAO.setWeatherCondition("humidity > 80");
        deviceStatusChangeDAO.setScenarioId(1L);

        DeviceStatusChangeDAO deviceStatusChangeDAO2 = new DeviceStatusChangeDAO();
        deviceStatusChangeDAO2.setDevice(deviceDAO);
        deviceStatusChangeDAO2.setOldStatus("OFF");
        deviceStatusChangeDAO2.setNewStatus("OPEN");
        deviceStatusChangeDAO2.setWeatherCondition("weather id = 800");
        deviceStatusChangeDAO2.setScenarioId(2L);

        WeatherDataDTO weatherData = new WeatherDataDTO();
        weatherData.setHumidity(97);

        WeatherDataDTO weatherData2 = new WeatherDataDTO();
        weatherData2.setWeatherIds(List.of(800));

        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findAll()).thenReturn(List.of(deviceDAO, deviceDAO2));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(deviceDAO));
        when(deviceRepository.findById(2L)).thenReturn(Optional.of(deviceDAO2));
        when(weatherDataClient.getWeatherData(40.7128, -74.0060, token)).thenReturn(weatherData);
        when(weatherDataClient.getWeatherData(54.9097, 80.0055, token)).thenReturn(weatherData2);
        when(scenarioClient.getScenariosByDeviceTypeId(1L, token)).thenReturn(List.of(scenario));
        when(scenarioClient.getScenariosByDeviceTypeId(2L, token)).thenReturn(List.of(scenario2));
        when(deviceRepository.save(deviceDAO)).thenReturn(updatedDeviceDAO);
        when(deviceRepository.save(deviceDAO2)).thenReturn(updatedDeviceDAO2);
        when(deviceStatusChangeRepository.save(deviceStatusChangeDAO)).thenReturn(deviceStatusChangeDAO);
        when(deviceStatusChangeRepository.save(deviceStatusChangeDAO2)).thenReturn(deviceStatusChangeDAO2);

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Scenario evaluation and device status changes performed successfully."));

        verify(authClient, times(1)).validateToken(anyString());
        verify(scenarioClient, times(2)).getScenariosByDeviceTypeId(anyLong(), anyString());
        verify(weatherDataClient, times(2)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(deviceRepository, times(1)).findAll();
        verify(deviceRepository, times(4)).findById(anyLong());
        verify(deviceRepository, times(2)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(2)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testEvaluateScenariosAndPerformActions_InvalidToken() throws Exception {
        when(authClient.validateToken(anyString())).thenReturn(false);

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());

        verify(authClient, times(1)).validateToken(anyString());
        verify(scenarioClient, times(0)).getScenariosByDeviceTypeId(anyLong(), anyString());
        verify(weatherDataClient, times(0)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(deviceRepository, times(0)).findAll();
        verify(deviceRepository, times(0)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testEvaluateScenariosAndPerformActions_NoToken() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isForbidden());

        verify(authClient, times(0)).validateToken(anyString());
        verify(weatherDataClient, times(0)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(scenarioClient, times(0)).getScenariosByDeviceTypeId(anyLong(), anyString());
        verify(deviceRepository, times(0)).findAll();
        verify(deviceRepository, times(0)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testEvaluateScenariosAndPerformActions_WeatherDataClientReturnsBadRequest() throws Exception {
        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setDeviceId(1L);
        deviceDAO.setDeviceType(new DeviceTypeDAO(101L, "Thermostat"));
        deviceDAO.setStatus("OFF");
        deviceDAO.setLatitude(40.7128);
        deviceDAO.setLongitude(-74.0060);

        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findAll()).thenReturn(List.of(deviceDAO));
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(deviceDAO));
        when(weatherDataClient.getWeatherData(anyDouble(), anyDouble(), anyString())).thenThrow(new BadRequestException("No weather data found."));

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No weather data found."));

        verify(authClient, times(1)).validateToken(anyString());
        verify(weatherDataClient, times(1)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(scenarioClient, times(0)).getScenariosByDeviceTypeId(anyLong(), anyString());
        verify(deviceRepository, times(1)).findAll();
        verify(deviceRepository, times(0)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testEvaluateScenariosAndPerformActionsWithSeveralDevicesAndScenarios_WeatherDataClientThrowsExceptionForSecondDevice() throws Exception {
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setScenarioId(1L);
        scenario.setDeviceTypeId(1L);
        scenario.setWeatherCondition("wind speed");
        scenario.setOperator("<");
        scenario.setConditionValue("20");
        scenario.setNewStatus("ON");

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setDeviceId(1L);
        deviceDAO.setDeviceType(new DeviceTypeDAO(1L, "Thermostat"));
        deviceDAO.setStatus("OFF");
        deviceDAO.setLatitude(40.7128);
        deviceDAO.setLongitude(-74.0060);

        DeviceDAO deviceDAO2 = new DeviceDAO();
        deviceDAO2.setDeviceId(2L);
        deviceDAO2.setDeviceType(new DeviceTypeDAO(2L, "Smart Light"));
        deviceDAO2.setStatus("OFF");
        deviceDAO2.setLatitude(54.9097);
        deviceDAO2.setLongitude(80.0055);

        DeviceDAO updatedDeviceDAO = new DeviceDAO();
        updatedDeviceDAO.setDeviceId(1L);
        updatedDeviceDAO.setDeviceType(new DeviceTypeDAO(1L, "Thermostat"));
        updatedDeviceDAO.setStatus("ON");
        updatedDeviceDAO.setLatitude(40.7128);
        updatedDeviceDAO.setLongitude(-74.0060);

        DeviceStatusChangeDAO deviceStatusChangeDAO = new DeviceStatusChangeDAO();
        deviceStatusChangeDAO.setDevice(deviceDAO);
        deviceStatusChangeDAO.setOldStatus("OFF");
        deviceStatusChangeDAO.setNewStatus("ON");
        deviceStatusChangeDAO.setWeatherCondition("wind speed < 20");
        deviceStatusChangeDAO.setScenarioId(1L);

        WeatherDataDTO weatherData = new WeatherDataDTO();
        weatherData.setWindSpeed(15.0);

        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findAll()).thenReturn(List.of(deviceDAO, deviceDAO2));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(deviceDAO));
        when(deviceRepository.findById(2L)).thenReturn(Optional.of(deviceDAO2));
        when(weatherDataClient.getWeatherData(40.7128, -74.0060, token)).thenReturn(weatherData);
        when(weatherDataClient.getWeatherData(54.9097, 80.0055, token)).thenThrow(new BadRequestException("No weather data found."));
        when(scenarioClient.getScenariosByDeviceTypeId(1L, token)).thenReturn(List.of(scenario));
        when(deviceRepository.save(deviceDAO)).thenReturn(updatedDeviceDAO);
        when(deviceStatusChangeRepository.save(deviceStatusChangeDAO)).thenReturn(deviceStatusChangeDAO);

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No weather data found."));

        verify(authClient, times(1)).validateToken(anyString());
        verify(scenarioClient, times(1)).getScenariosByDeviceTypeId(anyLong(), anyString());
        verify(weatherDataClient, times(2)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(deviceRepository, times(1)).findAll();
        verify(deviceRepository, times(2)).findById(anyLong());
        verify(deviceRepository, times(1)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(1)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testEvaluateScenariosAndPerformActionsWithSeveralDevicesAndScenarios_ScenarioClientThrowsExceptionForFirstDevice() throws Exception {
        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setDeviceId(1L);
        deviceDAO.setDeviceType(new DeviceTypeDAO(1L, "Thermostat"));
        deviceDAO.setStatus("OFF");
        deviceDAO.setLatitude(40.7128);
        deviceDAO.setLongitude(-74.0060);

        DeviceDAO deviceDAO2 = new DeviceDAO();
        deviceDAO2.setDeviceId(2L);
        deviceDAO2.setDeviceType(new DeviceTypeDAO(2L, "Smart Light"));
        deviceDAO2.setStatus("OFF");
        deviceDAO2.setLatitude(54.9097);
        deviceDAO2.setLongitude(80.0055);

        WeatherDataDTO weatherData = new WeatherDataDTO();
        weatherData.setTemperature(25.0);

        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findAll()).thenReturn(List.of(deviceDAO, deviceDAO2));
        when(weatherDataClient.getWeatherData(40.7128, -74.0060, token)).thenReturn(weatherData);
        when(scenarioClient.getScenariosByDeviceTypeId(1L, token)).thenThrow(new BadRequestException("Error getting scenarios."));

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error getting scenarios."));

        verify(authClient, times(1)).validateToken(anyString());
        verify(scenarioClient, times(1)).getScenariosByDeviceTypeId(anyLong(), anyString());
        verify(weatherDataClient, times(1)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(deviceRepository, times(1)).findAll();
        verify(deviceRepository, times(0)).findById(anyLong());
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testEvaluateScenariosAndPerformActions_Failure() throws Exception {
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setWeatherCondition("cloudiness");
        scenario.setDeviceTypeId(101L);
        scenario.setOperator("=");
        scenario.setConditionValue("60");
        scenario.setNewStatus("ON");
        scenario.setScenarioId(200L);

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setDeviceId(1L);
        deviceDAO.setDeviceType(new DeviceTypeDAO(101L, "Thermostat"));
        deviceDAO.setStatus("OFF");
        deviceDAO.setLatitude(40.7128);
        deviceDAO.setLongitude(-74.0060);

        DeviceDAO updatedDeviceDAO = new DeviceDAO();
        updatedDeviceDAO.setDeviceId(1L);
        updatedDeviceDAO.setDeviceType(new DeviceTypeDAO(101L, "Thermostat"));
        updatedDeviceDAO.setStatus("ON");
        updatedDeviceDAO.setLatitude(40.7128);
        updatedDeviceDAO.setLongitude(-74.0060);

        WeatherDataDTO weatherData = new WeatherDataDTO();
        weatherData.setCloudiness(60);

        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findAll()).thenReturn(List.of(deviceDAO));
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(deviceDAO));
        when(scenarioClient.getScenariosByDeviceTypeId(anyLong(), anyString())).thenReturn(List.of(scenario));
        when(weatherDataClient.getWeatherData(anyDouble(), anyDouble(), anyString())).thenReturn(weatherData);
        when(deviceRepository.save(any(DeviceDAO.class))).thenThrow(new RuntimeException("An unexpected error occurred"));

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred. Please try again later."));

        verify(authClient, times(1)).validateToken(anyString());
        verify(scenarioClient, times(1)).getScenariosByDeviceTypeId(anyLong(), anyString());
        verify(weatherDataClient, times(1)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(deviceRepository, times(1)).findAll();
        verify(deviceRepository, times(2)).findById(anyLong());
        verify(deviceRepository, times(1)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

    @Test
    void testEvaluateScenariosAndPerformActions_UnknownOperator() throws Exception {
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setWeatherCondition("sunrise");
        scenario.setDeviceTypeId(101L);
        scenario.setOperator("invalid-operator");  // Unknown operator
        scenario.setConditionValue("1661834187");
        scenario.setNewStatus("ON");
        scenario.setScenarioId(200L);

        DeviceDAO deviceDAO = new DeviceDAO();
        deviceDAO.setDeviceId(1L);
        deviceDAO.setDeviceType(new DeviceTypeDAO(101L, "Thermostat"));
        deviceDAO.setStatus("OFF");
        deviceDAO.setLatitude(40.7128);
        deviceDAO.setLongitude(-74.0060);

        WeatherDataDTO weatherData = new WeatherDataDTO();
        weatherData.setSunrise(LocalDateTime.now());

        when(authClient.validateToken(anyString())).thenReturn(true);
        when(deviceRepository.findAll()).thenReturn(List.of(deviceDAO));
        when(deviceRepository.findById(anyLong())).thenReturn(Optional.of(deviceDAO));
        when(scenarioClient.getScenariosByDeviceTypeId(anyLong(), anyString())).thenReturn(List.of(scenario));
        when(weatherDataClient.getWeatherData(anyDouble(), anyDouble(), anyString())).thenReturn(weatherData);

        mockMvc.perform(get(URL)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Scenario evaluation and device status changes performed successfully."));

        verify(authClient, times(1)).validateToken(anyString());
        verify(scenarioClient, times(1)).getScenariosByDeviceTypeId(anyLong(), anyString());
        verify(weatherDataClient, times(1)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(deviceRepository, times(1)).findAll();
        //Verify device update and status change are not saved because of illegal operator;
        verify(deviceRepository, times(0)).save(any(DeviceDAO.class));
        verify(deviceStatusChangeRepository, times(0)).save(any(DeviceStatusChangeDAO.class));
    }

}
