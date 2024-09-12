package com.smart.home.scenarioservice.mapper;

import com.smart.home.scenarioservice.model.Scenario;
import com.smart.home.scenarioservice.repository.model.ScenarioDAO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ScenarioMapper {

    ScenarioDAO scenarioToScenarioDAO(Scenario scenario);

    Scenario scenarioDAOToScenario(ScenarioDAO scenarioDAO);

}
