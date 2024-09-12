package com.smart.home.deviceservice.model;

import com.smart.home.deviceservice.swagger.DescriptionVariables;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatusChange {

    @Schema(description = "The database generated device status change id")
    @Range(min = 1, max = Long.MAX_VALUE, message = DescriptionVariables.MODEL_ID_RANGE)
    private Long id;

    @Schema(description = "Device associated with this status change")
    @NotNull(message = "Device ID must not be null")
    private Long device;

    @Schema(description = "Status of device before status change", example = "OFF")
    private String oldStatus;

    @Schema(description = "New status of device", example = "ON")
    @NotBlank(message = "Status " + DescriptionVariables.NOT_BLANK)
    private String newStatus;

    @Schema(description = "Time of device status change")
    private LocalDateTime changedAt;

    @Schema(description = "Weather condition, related to status change", example = "RAIN")
    private String weatherCondition;

    @Schema(description = "Scenario, related to status change")
    private Long scenarioId;

}
