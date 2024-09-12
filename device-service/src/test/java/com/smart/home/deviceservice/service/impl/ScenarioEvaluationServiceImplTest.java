package com.smart.home.deviceservice.service.impl;

import com.smart.home.deviceservice.client.ScenarioClient;
import com.smart.home.deviceservice.client.WeatherDataClient;
import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.model.dto.ScenarioDTO;
import com.smart.home.deviceservice.model.dto.WeatherDataDTO;
import com.smart.home.deviceservice.service.DeviceService;
import com.smart.home.deviceservice.service.DeviceStatusChangeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenarioEvaluationServiceImplTest {

    @Mock
    private DeviceService deviceService;

    @Mock
    private WeatherDataClient weatherDataClient;

    @Mock
    private ScenarioClient scenarioClient;

    @Mock
    private DeviceStatusChangeService deviceStatusChangeService;

    @InjectMocks
    private ScenarioEvaluationServiceImpl scenarioEvaluationService;

    @Test
    void evaluateAndPerformDeviceStatusChange_Success() {
        Device device = new Device();
        device.setDeviceId(1L);
        device.setDeviceType(101L);
        device.setLatitude(40.7128);
        device.setLongitude(-74.0060);
        String token = "Bearer token";
        when(deviceService.getAllDevices()).thenReturn(Collections.singletonList(device));

        WeatherDataDTO weatherData = new WeatherDataDTO();
        weatherData.setTemperature(25.0);
        when(weatherDataClient.getWeatherData(device.getLatitude(), device.getLongitude(), token)).thenReturn(weatherData);

        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setWeatherCondition("temperature");
        scenario.setDeviceTypeId(101L);
        scenario.setOperator(">");
        scenario.setConditionValue("20");
        scenario.setNewStatus("ON");
        scenario.setScenarioId(200L);
        when(scenarioClient.getScenariosByDeviceTypeId(device.getDeviceType(), token)).thenReturn(List.of(scenario));

        scenarioEvaluationService.evaluateScenario(scenario, weatherData);

        when(deviceStatusChangeService.changeDeviceStatus(anyLong(), anyString(), anyString(), anyLong())).thenThrow(BadRequestException.class);

        scenarioEvaluationService.evaluateAndPerformDeviceStatusChange(token);

        verify(deviceService, times(1)).getAllDevices();
        verify(weatherDataClient, times(1)).getWeatherData(device.getLatitude(), device.getLongitude(), token);
        verify(scenarioClient, times(1)).getScenariosByDeviceTypeId(device.getDeviceType(), token);
        verify(deviceStatusChangeService, times(1)).changeDeviceStatus(anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    void evaluateAndPerformDeviceStatusChange_NoMatchingScenarios() {
        Device device = new Device();
        device.setDeviceType(1L);
        device.setLatitude(67.0);
        device.setLongitude(20.0);
        String token = "Bearer token";
        when(deviceService.getAllDevices()).thenReturn(List.of(device));

        WeatherDataDTO weatherData = new WeatherDataDTO();
        when(weatherDataClient.getWeatherData(anyDouble(), anyDouble(), anyString())).thenReturn(weatherData);

        when(scenarioClient.getScenariosByDeviceTypeId(anyLong(), anyString())).thenReturn(Collections.emptyList());

        scenarioEvaluationService.evaluateAndPerformDeviceStatusChange(token);

        verify(deviceService, times(1)).getAllDevices();
        verify(weatherDataClient, times(1)).getWeatherData(anyDouble(), anyDouble(), anyString());
        verify(scenarioClient, times(1)).getScenariosByDeviceTypeId(1L, token);
        verify(deviceStatusChangeService, times(0)).changeDeviceStatus(anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    void evaluateScenario_TemperatureScenario_Success() {
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setWeatherCondition("temperature");
        scenario.setOperator(">");
        scenario.setConditionValue("20");

        WeatherDataDTO weatherData = new WeatherDataDTO();
        weatherData.setTemperature(25.0);

        boolean result = scenarioEvaluationService.evaluateScenario(scenario, weatherData);

        assertTrue(result);
    }

    @Test
    void evaluateScenario_UnknownWeatherCondition_ThrowsIllegalArgumentException() {
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setWeatherCondition("unknown");

        WeatherDataDTO weatherData = new WeatherDataDTO();

        assertThrows(IllegalArgumentException.class, () -> scenarioEvaluationService.evaluateScenario(scenario, weatherData));
    }

    @Test
    void evaluateScenario_WeatherConditionTests() {
        // Test for weather id
        ScenarioDTO scenario1 = new ScenarioDTO();
        scenario1.setWeatherCondition("weather id");
        scenario1.setConditionValue("800"); // Clear sky
        scenario1.setOperator("=");

        WeatherDataDTO weatherData1 = new WeatherDataDTO();
        weatherData1.setWeatherIds(List.of(800));

        assertTrue(scenarioEvaluationService.evaluateScenario(scenario1, weatherData1));

        // Test for weather description
        ScenarioDTO scenario2 = new ScenarioDTO();
        scenario2.setWeatherCondition("weather description");
        scenario2.setConditionValue("clear sky");
        scenario2.setOperator("=");

        WeatherDataDTO weatherData2 = new WeatherDataDTO();
        weatherData2.setWeatherDescriptions(List.of("clear sky"));

        assertTrue(scenarioEvaluationService.evaluateScenario(scenario2, weatherData2));

        // Test for humidity with greater than condition
        ScenarioDTO scenario3 = new ScenarioDTO();
        scenario3.setWeatherCondition("humidity");
        scenario3.setConditionValue("50");
        scenario3.setOperator(">");

        WeatherDataDTO weatherData3 = new WeatherDataDTO();
        weatherData3.setHumidity(60);

        assertTrue(scenarioEvaluationService.evaluateScenario(scenario3, weatherData3));

        // Test for windSpeed with less than condition
        ScenarioDTO scenario4 = new ScenarioDTO();
        scenario4.setWeatherCondition("wind speed");
        scenario4.setConditionValue("10.0");
        scenario4.setOperator("<");

        WeatherDataDTO weatherData4 = new WeatherDataDTO();
        weatherData4.setWindSpeed(5.0);

        assertTrue(scenarioEvaluationService.evaluateScenario(scenario4, weatherData4));

        // Test for cloudiness with equals condition returning false
        ScenarioDTO scenario5 = new ScenarioDTO();
        scenario5.setWeatherCondition("cloudiness");
        scenario5.setConditionValue("0");
        scenario5.setOperator("=");

        WeatherDataDTO weatherData5 = new WeatherDataDTO();
        weatherData5.setCloudiness(20);

        assertFalse(scenarioEvaluationService.evaluateScenario(scenario5, weatherData5));
    }

    @Test
    void evaluateSunrise_SunsetTest(){
        // Test for evaluating sunrise
        ScenarioDTO scenarioAfterSunrise = new ScenarioDTO();
        scenarioAfterSunrise.setWeatherCondition("sunrise");
        scenarioAfterSunrise.setOperator(">");

        LocalDateTime sunriseTime =  LocalDateTime.now().minusHours(1);

        assertTrue(scenarioEvaluationService.evaluateSunrise(scenarioAfterSunrise, sunriseTime));

        // Test for evaluating sunrise
        ScenarioDTO scenarioAtSunset = new ScenarioDTO();
        scenarioAtSunset.setWeatherCondition("sunset");
        scenarioAtSunset.setOperator("=");

        LocalDateTime sunsetTime = LocalDateTime.now();

        assertTrue(scenarioEvaluationService.evaluateSunset(scenarioAtSunset, sunsetTime));

        ScenarioDTO scenarioBeforeSunset = new ScenarioDTO();
        scenarioBeforeSunset.setWeatherCondition("sunset");
        scenarioBeforeSunset.setOperator("<");

        sunsetTime = LocalDateTime.now().plusHours(1);  // Sunset is one hour from now

        assertTrue(scenarioEvaluationService.evaluateSunset(scenarioBeforeSunset, sunsetTime));

    }

    @Test
    void evaluateScenario_UnknownWeatherCondition_ThrowsException() {
        ScenarioDTO scenario = new ScenarioDTO();
        scenario.setWeatherCondition("unknownCondition");
        scenario.setConditionValue("10");
        scenario.setOperator("=");

        WeatherDataDTO weatherData = new WeatherDataDTO();

        assertThrows(IllegalArgumentException.class, () -> {
            scenarioEvaluationService.evaluateScenario(scenario, weatherData);
        });
    }

}
