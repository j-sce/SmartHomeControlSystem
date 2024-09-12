package com.smart.home.deviceservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScenarioDTO {

    @Schema(description = "The database generated device id")
    private Long scenarioId;

    @Schema(description = "ID of device type from database", example = "1")
    private Long deviceTypeId;

    @Schema(description = "Weather condition, on which based scenario", example = "temperature")
    private String weatherCondition;

    @Schema(description = "Value of weather condition", example = "25")
    private String conditionValue;

    @Schema(description = "Operator for the condition (>, <, =)", example = ">")
    private String operator;

    @Schema(description = "New status of device if scenario is reached", example = "ON")
    private String newStatus;

}
