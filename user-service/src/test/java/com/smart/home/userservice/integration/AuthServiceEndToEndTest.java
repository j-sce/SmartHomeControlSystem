package com.smart.home.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.userservice.model.LoginDTO;
import com.smart.home.userservice.model.TokenValidationRequest;
import com.smart.home.userservice.model.User;
import com.smart.home.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("localhost")
@AutoConfigureMockMvc
public class AuthServiceEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void login_Success() throws Exception {
        User user = new User("validUser", "validEmail", "$2a$12$qgN1u6j9M5iDkq6IGUWw0.Dmwj1vr7GfYuHq5F3HM5GLNkY1j1DE6");
        user.setRoles(Set.of());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        LoginDTO loginDTO = new LoginDTO("validUser", "password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isString());
    }

    @Test
    void login_InvalidPassword() throws Exception {
        User user = new User("validUser", "validEmail", "$2a$12$qgN1u6j9M5iDkq6IGUWw0.Dmwj1vr7GfYuHq5F3HM5GLNkY1j1DE6");
        user.setRoles(Set.of());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        LoginDTO loginDTO = new LoginDTO("validUser", "wrongPassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_UserNotFound() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        LoginDTO loginDTO = new LoginDTO("nonExistentUser", "password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_Success() throws Exception {
        User user = new User("newUser", "newEmail@test.com", "ComplexP@ss1");

        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.email").value("newEmail@test.com"));
    }

    @Test
    void register_InvalidFields() throws Exception {
        User user = new User("", "", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        "username: Username must be between 1 and 50 characters",
                        "username: Username must not be blank",
                        "password: Password must be between 8 and 100 characters",
                        "password: Password must not be blank",
                        "email: Email local part allows: - Numeric values from 0 to 9. - Uppercase and lowercase letters from a to z. - Underscore “_”, hyphen “-“, and dot “.” - Dot isn’t allowed at the start and end of the local part. - Consecutive dots aren’t allowed. - A maximum of 64 characters are allowed. Email domain part allows: - Numeric values from 0 to 9. - Uppercase and lowercase letters from a to z. - Hyphen “-” and dot “.” aren’t allowed at the start and end of the domain part. - Consecutive dots aren’t allowed.",
                        "email: Email must not be null")));
    }

    @Test
    void register_UsernameNotUnique() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(new User()));

        User user = new User("existingUser", "newEmail@test.com", "ComplexP@ss1");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Username is already taken"));
    }

    @Test
    void register_EmailNotUnique() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        User user = new User("newUser", "existingEmail@test.com", "ComplexP@ss1");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Email is already registered"));
    }

    @Test
    void register_PasswordNotComplex() throws Exception {
        User user = new User("newUser", "newEmail@test.com", "simplepass");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Password must be at least 8 characters long and contain " +
                        "at least one uppercase letter, one lowercase letter, one digit, and one special character."));
    }

    @Test
    void validateToken_ValidToken() throws Exception {
        User user = new User("testuser", "validEmail", "$2a$12$IETFUcW.CaFdEe3/PhOGxu4UDYGiL84U0iWTXT7DKLX4fYskOEZ/i");
        user.setRoles(Set.of());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        TokenValidationRequest request = new TokenValidationRequest("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGVzIjpbXSwiaWF0IjoxNzI2ODI4NDQ3LCJleHAiOjE3MjY5MTQ4NDd9.vje0CunZBhfVb9y7TwYHzBo6Vka2t73tniJsH6ANkbo");

        mockMvc.perform(post("/api/auth/token/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void validateToken_UserNotFound() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        TokenValidationRequest request = new TokenValidationRequest("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsInJvbGVzIjpbXSwiaWF0IjoxNzI2ODI4NDQ3LCJleHAiOjE3MjY5MTQ4NDd9.vje0CunZBhfVb9y7TwYHzBo6Vka2t73tniJsH6ANkbo");

        mockMvc.perform(post("/api/auth/token/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void validateToken_NoToken() throws Exception {
        TokenValidationRequest request = new TokenValidationRequest();

        mockMvc.perform(post("/api/auth/token/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

}
