package com.smart.home.scenarioservice.model;

import com.smart.home.scenarioservice.swagger.DescriptionVariables;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class Scenario {

    @Schema(description = "The database generated device id")
    @Range(min = 1, max = Long.MAX_VALUE, message = DescriptionVariables.MODEL_ID_RANGE)
    private Long scenarioId;

    @Schema(description = "ID of device type from database", example = "1")
    @NotNull(message = "Device type id " + DescriptionVariables.NOT_NULL)
    @Range(min = 1, max = Long.MAX_VALUE, message = DescriptionVariables.MODEL_ID_RANGE)
    private Long deviceTypeId;

    @Schema(description = "Weather condition, on which based scenario", example = "temperature")
    @NotBlank(message = "Weather condition " + DescriptionVariables.NOT_BLANK)
    private String weatherCondition;

    @Schema(description = "Value of weather condition", example = "25")
    @NotBlank(message = "Weather condition value " + DescriptionVariables.NOT_BLANK)
    private String conditionValue;

    @Schema(description = "Operator for the condition (>, <, =)", example = ">")
    @NotBlank(message = "Weather condition value " + DescriptionVariables.NOT_BLANK)
    private String operator;

    @Schema(description = "New status of device if scenario is reached", example = "ON")
    @NotBlank(message = "Status " + DescriptionVariables.NOT_BLANK)
    private String newStatus;

}
