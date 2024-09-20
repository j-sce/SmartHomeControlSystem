package com.smart.home.deviceservice.service.impl;

import com.smart.home.deviceservice.client.ScenarioClient;
import com.smart.home.deviceservice.client.WeatherDataClient;
import com.smart.home.deviceservice.handler.BadRequestException;
import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.model.dto.ScenarioDTO;
import com.smart.home.deviceservice.model.dto.WeatherDataDTO;
import com.smart.home.deviceservice.service.DeviceService;
import com.smart.home.deviceservice.service.DeviceStatusChangeService;
import com.smart.home.deviceservice.service.ScenarioEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ScenarioEvaluationServiceImpl implements ScenarioEvaluationService {

    private final DeviceService deviceService;
    private final WeatherDataClient weatherDataClient;
    private final ScenarioClient scenarioClient;
    private final DeviceStatusChangeService deviceStatusChangeService;


    @Override
    public void evaluateAndPerformDeviceStatusChange(String token) {
        List<Device> devices = deviceService.getAllDevices();

        for (Device device : devices) {
            WeatherDataDTO weatherData = weatherDataClient.getWeatherData(device.getLatitude(), device.getLongitude(), token);
            List<ScenarioDTO> scenarios = scenarioClient.getScenariosByDeviceTypeId(device.getDeviceType(), token);

            for (ScenarioDTO scenario : scenarios) {
                try {
                    if (evaluateScenario(scenario, weatherData)) {
                        deviceStatusChangeService.changeDeviceStatus(
                                device.getDeviceId(),
                                scenario.getNewStatus(),
                                scenario.getWeatherCondition() + " " + scenario.getOperator() + " " + scenario.getConditionValue(),
                                scenario.getScenarioId()
                        );
                    }
                } catch (IllegalArgumentException | BadRequestException e) {
                    log.error("Error evaluating scenario for device {}: {}", device.getDeviceId(), e.getMessage(), e);
                    continue;
                }
            }
        }
    }

    @Override
    public boolean evaluateScenario(ScenarioDTO scenario, WeatherDataDTO weatherData) {

        return switch (scenario.getWeatherCondition()) {
            case "weather id" -> evaluateWeatherId(scenario, weatherData.getWeatherIds());
            case "weather description" -> evaluateWeatherDescription(scenario, weatherData.getWeatherDescriptions());
            case "temperature" -> evaluateTemperature(scenario, weatherData.getTemperature());
            case "humidity" -> evaluateHumidity(scenario, weatherData.getHumidity());
            case "wind speed" -> evaluateWindSpeed(scenario, weatherData.getWindSpeed());
            case "cloudiness" -> evaluateCloudiness(scenario, weatherData.getCloudiness());
            case "sunrise" -> evaluateSunrise(scenario, weatherData.getSunrise());
            case "sunset" -> evaluateSunset(scenario, weatherData.getSunset());
            default ->
                    throw new IllegalArgumentException("Unknown weather condition: " + scenario.getWeatherCondition());
        };
    }

    @Override
    public boolean evaluateWeatherId(ScenarioDTO scenario, List<Integer> weatherIds) {
        int conditionValue = Integer.parseInt(scenario.getConditionValue());
        return weatherIds.contains(conditionValue);
    }

    @Override
    public boolean evaluateWeatherDescription(ScenarioDTO scenario, List<String> descriptions) {
        return descriptions.contains(scenario.getConditionValue());
    }

    @Override
    public boolean evaluateTemperature(ScenarioDTO scenario, Double temperature) {
        double conditionValue = Double.parseDouble(scenario.getConditionValue());
        return evaluateCondition(temperature, conditionValue, scenario.getOperator());
    }

    @Override
    public boolean evaluateHumidity(ScenarioDTO scenario, Integer humidity) {
        int conditionValue = Integer.parseInt(scenario.getConditionValue());
        return evaluateCondition(humidity, conditionValue, scenario.getOperator());
    }

    @Override
    public boolean evaluateWindSpeed(ScenarioDTO scenario, Double windSpeed) {
        double conditionValue = Double.parseDouble(scenario.getConditionValue());
        return evaluateCondition(windSpeed, conditionValue, scenario.getOperator());
    }

    @Override
    public boolean evaluateCloudiness(ScenarioDTO scenario, Integer cloudiness) {
        int conditionValue = Integer.parseInt(scenario.getConditionValue());
        return evaluateCondition(cloudiness, conditionValue, scenario.getOperator());
    }

    @Override
    public boolean evaluateSunrise(ScenarioDTO scenario, LocalDateTime sunrise) {
        return evaluateCondition(LocalDateTime.now(), sunrise, scenario.getOperator());
    }

    @Override
    public boolean evaluateSunset(ScenarioDTO scenario, LocalDateTime sunset) {
        return evaluateCondition(LocalDateTime.now(), sunset, scenario.getOperator());
    }


    private <T extends Comparable<T>> boolean evaluateCondition(T actualValue, T conditionValue, String operator) {
        return switch (operator) {
            case ">" -> actualValue.compareTo(conditionValue) > 0;
            case "<" -> actualValue.compareTo(conditionValue) < 0;
            case "=" -> actualValue.compareTo(conditionValue) == 0;
            default -> throw new IllegalArgumentException("Unknown scenario operator: " + operator);
        };
    }

}
