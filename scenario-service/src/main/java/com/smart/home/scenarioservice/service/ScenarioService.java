package com.smart.home.scenarioservice.service;

import com.smart.home.scenarioservice.model.Scenario;

import java.util.List;

public interface ScenarioService {

    Scenario addScenario(Scenario scenario, String token);

    Scenario updateScenarioById(Long scenarioId, Scenario scenario, String token);

    void deleteScenarioById(Long scenarioId);

    Scenario getScenarioById(Long scenarioId);

    List<Scenario> getAllScenarios();

    List<Scenario> getScenariosByDeviceTypeId(Long deviceTypeId, String token);

}
