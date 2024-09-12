package com.smart.home.deviceservice.service;

import com.smart.home.deviceservice.model.dto.ScenarioDTO;
import com.smart.home.deviceservice.model.dto.WeatherDataDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ScenarioEvaluationService {

    void evaluateAndPerformDeviceStatusChange(String token);

    boolean evaluateScenario(ScenarioDTO scenario, WeatherDataDTO weatherData);

    boolean evaluateWeatherId(ScenarioDTO scenario, List<Integer> weatherIds);

    boolean evaluateWeatherDescription(ScenarioDTO scenario, List<String> descriptions);

    boolean evaluateTemperature(ScenarioDTO scenario, Double temperature);

    boolean evaluateHumidity(ScenarioDTO scenario, Integer humidity);

    boolean evaluateWindSpeed(ScenarioDTO scenario, Double windSpeed);

    boolean evaluateCloudiness(ScenarioDTO scenario, Integer cloudiness);

    boolean evaluateSunrise(ScenarioDTO scenario, LocalDateTime sunrise);

    boolean evaluateSunset(ScenarioDTO scenario, LocalDateTime sunset);

}
