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
public class Device {

    @Schema(description = "The database generated device id")
    @Range(min = 1, max = Long.MAX_VALUE, message = DescriptionVariables.MODEL_ID_RANGE)
    private Long deviceId;

    @Schema(description = "Name of device")
    @NotBlank(message = "Device name " + DescriptionVariables.NOT_BLANK)
    private String deviceName;

    @Schema(description = "ID of device type from database", example = "1")
    @NotNull(message = "Device type id " + DescriptionVariables.NOT_NULL)
    @Range(min = 1, max = Long.MAX_VALUE, message = DescriptionVariables.MODEL_ID_RANGE)
    private Long deviceType;

    @Schema(description = "Latitude of device location", example = "56.9710")
    @NotNull(message = DescriptionVariables.LATITUDE_RANGE)
    @Range(min = -90, max = 90, message = DescriptionVariables.LATITUDE_RANGE)
    private Double latitude;

    @Schema(description = "Longitude of device location", example = "24.1604")
    @NotNull(message = DescriptionVariables.LONGITUDE_RANGE)
    @Range(min = -180, max = 180, message = DescriptionVariables.LONGITUDE_RANGE)
    private Double longitude;

    @Schema(description = "Status of device", example = "OFF")
    @NotBlank(message = "Device status " + DescriptionVariables.NOT_BLANK)
    private String status;

    @Schema(description = "Time of last device update")
    private LocalDateTime lastUpdated;

}
