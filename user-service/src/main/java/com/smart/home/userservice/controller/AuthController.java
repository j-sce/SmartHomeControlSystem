package com.smart.home.userservice.controller;

import com.smart.home.userservice.model.LoginDTO;
import com.smart.home.userservice.model.TokenValidationRequest;
import com.smart.home.userservice.model.User;
import com.smart.home.userservice.service.UserService;
import com.smart.home.userservice.swagger.DescriptionVariables;
import com.smart.home.userservice.swagger.HTTPResponseMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("/api/auth")
@Tag(name = DescriptionVariables.AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Logs in user, returns authentication token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        log.info("Attempting to log in user with username: {}", loginDTO.getUsername());
        String token = userService.login(loginDTO);

        log.debug("User {} logged in successfully", loginDTO.getUsername());
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Creates new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HTTPResponseMessages.HTTP_201,
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user, BindingResult bindingResult) {
        log.info("Registering user with username: {}", user.getUsername());

        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult);
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        User createdUser = userService.createUser(user);
        log.debug("User {} registered successfully", user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "Validates token, returns validation result")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PostMapping("/token/validate")
    public ResponseEntity<Boolean> validateToken(@RequestBody TokenValidationRequest request) {
        log.info("Attempting to validate token: {}", request.getToken());
        boolean isValid = userService.validateToken(request);

        log.debug("Token {} validation completed.", request.getToken());
        return ResponseEntity.ok(isValid);
    }

}
