package com.smart.home.scenarioservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeviceTypeDTO {

    @Schema(description = "The database generated device type id")
    private Long id;

    @Schema(description = "Name of device type")
    private String name;

}
