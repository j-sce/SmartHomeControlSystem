package com.smart.home.deviceservice.controller;

import com.smart.home.deviceservice.model.DeviceType;
import com.smart.home.deviceservice.service.DeviceTypeService;
import com.smart.home.deviceservice.swagger.DescriptionVariables;
import com.smart.home.deviceservice.swagger.HTTPResponseMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("/api/device/type")
@Tag(name = DescriptionVariables.DEVICE_TYPE)
@RequiredArgsConstructor
public class DeviceTypeController {

    private final DeviceTypeService deviceTypeService;


    @Operation(summary = "Adds new device type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HTTPResponseMessages.HTTP_201,
                    content = @Content(schema = @Schema(implementation = DeviceType.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PostMapping
    public ResponseEntity<DeviceType> addDeviceType(
            @Parameter(name = "deviceType", description = "device type", example = "Smart Light", required = true) @RequestParam String deviceType) {
        log.info("Adding new device type: {}", deviceType);

        DeviceType addedDeviceType = deviceTypeService.addDeviceType(deviceType);
        log.debug("Device type added: {}", addedDeviceType);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedDeviceType);
    }

    @Operation(summary = "Updates device by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = DeviceType.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDeviceTypeById(@NonNull @PathVariable("id")
                                                  @Parameter(name = "id", description = "id of device type", example = "1", required = true) Long id,
                                                  @Parameter(name = "deviceType", description = "updated device type", example = "Thermostat", required = true) @RequestParam String deviceType) {
        log.info("Updating device type with id: {}", id);

        DeviceType updatedDeviceType = deviceTypeService.updateDeviceTypeById(id, deviceType);
        log.info("Device type with id: {} successfully updated", id);
        return ResponseEntity.status(HttpStatus.OK).body(updatedDeviceType);
    }

    @Operation(summary = "Deletes device type by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = HTTPResponseMessages.HTTP_204, content = @Content),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeviceTypeById(@NonNull @PathVariable("id")
                                                  @Parameter(name = "id", description = "id of device type", example = "1", required = true) Long id) {
        log.info("Deleting device type with id: {}", id);

        deviceTypeService.deleteDeviceTypeById(id);
        log.info("device type with id: {} successfully deleted", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gets device type by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = DeviceType.class))),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceTypeById(@NonNull @PathVariable("id")
                                               @Parameter(name = "id", description = "id of device type", example = "1", required = true) Long id) {
        log.info("Getting device type by id: {}", id);
        DeviceType deviceType =  deviceTypeService.getDeviceTypeById(id);

        log.debug("Device type with id {} is: {}", id, deviceType);
        return ResponseEntity.status(HttpStatus.OK).body(deviceType);
    }

    @Operation(summary = "Gets list of device types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeviceType.class)))),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<DeviceType>> getAllDeviceTypes() {
        log.info("Retrieving list of device types");

        List<DeviceType> deviceTypeList = deviceTypeService.getAllDeviceTypes();
        if (deviceTypeList.isEmpty()) {
            log.warn("Device type list is empty.");
            return ResponseEntity.ok(deviceTypeList);
        }
        log.debug("Found device type list. Size: {}", deviceTypeList.size());
        return ResponseEntity.ok(deviceTypeList);
    }

}
