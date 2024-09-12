package com.smart.home.deviceservice.controller;

import com.smart.home.deviceservice.model.Device;
import com.smart.home.deviceservice.service.DeviceService;
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
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/api/device")
@Tag(name = DescriptionVariables.DEVICE)
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;


    @Operation(summary = "Adds new device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HTTPResponseMessages.HTTP_201,
                    content = @Content(schema = @Schema(implementation = Device.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> addDevice(@Valid @RequestBody Device device, BindingResult bindingResult) {
        log.info("Adding new device: {}", device);

        ResponseEntity<?> validationResponse = validateBindingResult(bindingResult);
        if (validationResponse != null) {
            return validationResponse;
        }

        Device addedDevice = deviceService.addDevice(device);
        log.debug("Device added: {}", addedDevice);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedDevice);
    }

    @Operation(summary = "Updates device by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = Device.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDeviceById(@NonNull @PathVariable("id")
                                              @Parameter(name = "id", description = "id of device", example = "1", required = true) Long id,
                                              @Valid @RequestBody Device deviceUpdate,
                                              BindingResult bindingResult) {
        log.info("Updating device with id: {}", id);

        ResponseEntity<?> validationResponse = validateBindingResult(bindingResult);
        if (validationResponse != null) {
            return validationResponse;
        }

        Device updatedDevice = deviceService.updateDeviceById(id, deviceUpdate);
        log.info("Device with id: {} successfully updated", id);
        return ResponseEntity.ok(updatedDevice);
    }

    @Operation(summary = "Deletes device by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = HTTPResponseMessages.HTTP_204, content = @Content),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDeviceById(@NonNull @PathVariable("id")
                                              @Parameter(name = "id", description = "id of device", example = "1", required = true) Long id) {
        log.info("Deleting device with id: {}", id);

        deviceService.deleteDeviceById(id);
        log.info("device with id: {} successfully deleted", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gets device by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = Device.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceById(@NonNull @PathVariable("id")
                                           @Parameter(name = "id", description = "id of device", example = "1", required = true) Long id) {
        log.info("Getting device by id: {}", id);
        Device device = deviceService.getDeviceById(id);

        log.debug("Device with id {} is: {}", id, device);
        return ResponseEntity.status(HttpStatus.OK).body(device);
    }

    @Operation(summary = "Gets list of devices")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Device.class)))),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        log.info("Retrieving list of devices");
        List<Device> deviceList = deviceService.getAllDevices();

        if (deviceList.isEmpty()) {
            log.warn("Device list is empty.");
            return ResponseEntity.ok(deviceList);
        }
        log.debug("Found device list. Size: {}", deviceList.size());
        return ResponseEntity.ok(deviceList);
    }


    private ResponseEntity<?> validateBindingResult(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult);
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }
        return null;
    }

}
