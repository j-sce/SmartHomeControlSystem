package com.smart.home.userservice.controller;

import com.smart.home.userservice.model.User;
import com.smart.home.userservice.service.UserService;
import com.smart.home.userservice.swagger.DescriptionVariables;
import com.smart.home.userservice.swagger.HTTPResponseMessages;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@RestController
@RequestMapping("api/user")
@Tag(name = DescriptionVariables.USER)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Updates user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUserById(@NonNull @PathVariable("id")
                                            @Parameter(name = "id", description = "id of user", example = "1", required = true) Long id,
                                            @Valid @RequestBody User userUpdate,
                                            BindingResult bindingResult) {
        log.info("Updating user with id: {}", id);

        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult);
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        User updatedUser = userService.updateUserById(id, userUpdate);
        log.debug("User with id: {} updated successfully", id);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Deletes user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = HTTPResponseMessages.HTTP_204, content = @Content),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@NonNull @PathVariable("id")
                                                   @Parameter(name = "id", description = "id of user", example = "1", required = true) Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUserById(id);

        log.info("User with id: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Gets user by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = HTTPResponseMessages.HTTP_400, content = @Content),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@NonNull @PathVariable("id")
                                                @Parameter(name = "id", description = "id of user", example = "1", required = true) Long id) {
        log.info("Getting user by id: {}", id);
        User user = userService.getUserById(id);

        log.debug("User with id {} is: {}", id, user);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Gets list of users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = User.class)))),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Retrieving list of devices");
        List<User> users = userService.getAllUsers();

        log.debug("Found users list. Size: {}", users.size());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Gets authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HTTPResponseMessages.HTTP_200,
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "403", description = HTTPResponseMessages.HTTP_403, content = @Content),
            @ApiResponse(responseCode = "404", description = HTTPResponseMessages.HTTP_404, content = @Content),
            @ApiResponse(responseCode = "500", description = HTTPResponseMessages.HTTP_500, content = @Content)
    })
    @GetMapping("/profile")
    public ResponseEntity<User> userProfile() {
        log.info("Fetching profile for authenticated user");
        User profile = userService.getProfile();

        log.debug("Profile for authenticated user: {}", profile);
        return ResponseEntity.ok(profile);
    }

}
