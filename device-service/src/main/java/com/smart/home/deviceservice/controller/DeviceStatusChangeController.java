package com.smart.home.deviceservice.controller;

import com.smart.home.deviceservice.model.DeviceStatusChange;
import com.smart.home.deviceservice.service.DeviceStatusChangeService;
import com.smart.home.deviceservice.swagger.DescriptionVariables;
import com.smart.home.deviceservice.swagger.HTTPResponseMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/device/status")
@Tag(name = DescriptionVariables.DEVICE_STATUS)
@RequiredArgsConstructor
public class DeviceStatusChangeController {

    private final DeviceStatusChangeService deviceStatusChangeService;


    @Operation(summary = "Changes device status by device id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HTTPResponseMessages.HTTP_201,
                    content = @Content(schema = @Schema(implementation = DeviceStatusChange.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PatchMapping("/{id}")
    public ResponseEntity<?> changeDeviceStatus(
            @NonNull @PathVariable("id") @Parameter(name = "id", description = "id of device", example = "1", required = true) Long id,
            @Parameter(name = "status", description = "new status of device", example = "OFF", required = true) @RequestParam String status) {
        log.debug("Request to change status of device with id: {} to status: {}", id, status);
        DeviceStatusChange deviceStatusChange = deviceStatusChangeService.changeDeviceStatus(id, status, "manual status change", null);

        log.info("Status of device with id: {} successfully changed to: {}", id, status);
        return ResponseEntity.ok(deviceStatusChange);
    }

}
