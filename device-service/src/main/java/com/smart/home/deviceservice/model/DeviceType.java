package com.smart.home.deviceservice.model;

import com.smart.home.deviceservice.swagger.DescriptionVariables;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class DeviceType {

    @Schema(description = "The database generated device type id")
    private Long deviceTypeId;

    @Schema(description = "Name of device type")
    @NotBlank(message = "Device type name " + DescriptionVariables.NOT_BLANK)
    private String deviceTypeName;

}
