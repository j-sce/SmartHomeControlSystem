package com.smart.home.deviceservice.service.impl;

import com.smart.home.deviceservice.service.ScenarioEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class ScenarioEvaluationScheduler {

    private final ScenarioEvaluationService scenarioEvaluationService;


    @Scheduled(fixedRate = 60000) // runs every 60 seconds
    public void evaluateScenarios() {
        log.info("Evaluating scenarios.");
//        scenarioEvaluationService.evaluateAndPerformDeviceStatusChange(); TODO make it work (it doesn't because lacks Authorization)
    }

}
