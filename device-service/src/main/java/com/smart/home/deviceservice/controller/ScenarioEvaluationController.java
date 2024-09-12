package com.smart.home.deviceservice.controller;

import com.smart.home.deviceservice.service.ScenarioEvaluationService;
import com.smart.home.deviceservice.swagger.DescriptionVariables;
import com.smart.home.deviceservice.swagger.HTTPResponseMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/scenario-evaluation")
@Tag(name = DescriptionVariables.SCENARIO_EVALUATION)
@RequiredArgsConstructor
public class ScenarioEvaluationController {

    private final ScenarioEvaluationService scenarioEvaluationService;


    @Operation(summary = "Evaluates scenarios and performs device status changes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping("/evaluate")
    public ResponseEntity<String> evaluateScenariosAndPerformActions(@RequestHeader("Authorization") String token) {
        scenarioEvaluationService.evaluateAndPerformDeviceStatusChange(token);
        return ResponseEntity.ok("Scenario evaluation and device status changes performed successfully.");
    }

}
