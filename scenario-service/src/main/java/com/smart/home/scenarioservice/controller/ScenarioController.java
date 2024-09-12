package com.smart.home.scenarioservice.controller;

import com.smart.home.scenarioservice.model.Scenario;
import com.smart.home.scenarioservice.service.ScenarioService;
import com.smart.home.scenarioservice.swagger.DescriptionVariables;
import com.smart.home.scenarioservice.swagger.HTTPResponseMessages;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/api/scenario")
@Tag(name = DescriptionVariables.SCENARIO)
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService scenarioService;


    @Operation(summary = "Adds new scenario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HTTPResponseMessages.HTTP_201,
                    content = @Content(schema = @Schema(implementation = Scenario.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PostMapping
    public ResponseEntity<?> addScenario(@Valid @RequestBody Scenario scenario,
                                         @RequestHeader("Authorization") String token,
                                         BindingResult bindingResult) {
        log.info("Adding new scenario: {}", scenario);

        ResponseEntity<?> validationResponse = validateBindingResult(bindingResult);
        if (validationResponse != null) {
            return validationResponse;
        }

        Scenario addedScenario = scenarioService.addScenario(scenario, token);
        log.debug("Scenario added: {}", addedScenario);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedScenario);
    }

    @Operation(summary = "Updates scenario by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = Scenario.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateScenarioById(@NonNull @PathVariable("id")
                                                @Parameter(name = "id", description = "id of scenario", example = "1", required = true) Long id,
                                                @Valid @RequestBody Scenario scenarioUpdate,
                                                @RequestHeader("Authorization") String token,
                                                BindingResult bindingResult) {
        log.info("Updating scenario with id: {}", id);

        ResponseEntity<?> validationResponse = validateBindingResult(bindingResult);
        if (validationResponse != null) {
            return validationResponse;
        }

        Scenario updatedScenario = scenarioService.updateScenarioById(id, scenarioUpdate, token);
        log.info("Scenario with id: {} successfully updated", id);
        return ResponseEntity.status(HttpStatus.OK).body(updatedScenario);
    }

    @Operation(summary = "Deletes scenario by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = HTTPResponseMessages.HTTP_204, content = @Content),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScenarioById(@NonNull @PathVariable("id")
                                                @Parameter(name = "id", description = "id of scenario", example = "1", required = true) Long id) {
        log.info("Deleting scenario with id: {}", id);

        scenarioService.deleteScenarioById(id);
        log.info("Scenario with id: {} successfully deleted", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gets scenario by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = Scenario.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getScenarioById(@NonNull @PathVariable("id")
                                             @Parameter(name = "id", description = "id of scenario", example = "1", required = true) Long id) {
        log.info("Getting scenario by id: {}", id);
        Scenario scenario = scenarioService.getScenarioById(id);

        log.debug("Scenario with id {} is: {}", id, scenario);
        return ResponseEntity.status(HttpStatus.OK).body(scenario);
    }

    @Operation(summary = "Gets list of scenarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Scenario.class)))),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Scenario>> getAllScenarios() {
        log.info("Retrieving list of scenarios");
        List<Scenario> scenarioList = scenarioService.getAllScenarios();

        if (scenarioList.isEmpty()) {
            log.warn("Scenario list is empty.");
            return ResponseEntity.ok(scenarioList);
        }
        log.debug("Found scenario list. Size: {}", scenarioList.size());
        return ResponseEntity.ok(scenarioList);
    }

    @Operation(summary = "Gets list of scenarios by device type id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Scenario.class)))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping("/device")
    public ResponseEntity<List<Scenario>> getScenariosByDeviceTypeId(
            @Parameter(name = "deviceTypeId", description = "device type id", example = "1", required = true) @RequestParam Long deviceTypeId,
            @RequestHeader("Authorization") String token) {
        log.info("Retrieving list of scenarios by device type id");
        List<Scenario> scenarioList = scenarioService.getScenariosByDeviceTypeId(deviceTypeId, token);

        if (scenarioList.isEmpty()) {
            log.warn("Scenario list for provided device type id is empty.");
            return ResponseEntity.ok(scenarioList);
        }
        log.debug("Found scenarios by device type id. List size: {}", scenarioList.size());
        return ResponseEntity.ok(scenarioList);
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
