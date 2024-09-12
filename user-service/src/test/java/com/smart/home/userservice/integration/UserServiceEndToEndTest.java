package com.smart.home.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.userservice.model.User;
import com.smart.home.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void updateUserById_Success() throws Exception {
        User existingUser = new User();
        existingUser.setUserId(1L);
        existingUser.setUsername("User");
        existingUser.setEmail("existing@test.com");
        existingUser.setPassword("OldPassword123$");

        User userUpdate = new User();
        userUpdate.setUserId(1L);
        userUpdate.setUsername("UpdatedUser");
        userUpdate.setEmail("updated@test.com");
        userUpdate.setPassword("Password123$");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername("UpdatedUser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("updated@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(userUpdate);

        mockMvc.perform(put("/api/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("UpdatedUser")))
                .andExpect(jsonPath("$.email", is("updated@test.com")));
    }

    @Test
    @WithAnonymousUser
    void updateUserById_Unauthenticated() throws Exception {
        mockMvc.perform(put("/api/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new User())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateUserById_UserNotFound() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        User user = new User("user", "Email@test.com", "ComplexP@ss1");
        mockMvc.perform(put("/api/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("User not found with id: 1"));
    }

    @Test
    @WithMockUser
    void updateUserById_InvalidFields() throws Exception {
        User user = new User("", "", "");
        mockMvc.perform(put("/api/user/{id}", 1L)
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
    @WithMockUser
    void updateUserById_UsernameNotUnique() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(new User()));

        User user = new User("existingUsername", "newEmail@test.com", "ComplexP@ss1");
        mockMvc.perform(put("/api/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Username is already taken"));
    }

    @Test
    @WithMockUser
    void updateUserById_EmailNotUnique() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        User user = new User("newUser", "existingEmail@test.com", "ComplexP@ss1");
        mockMvc.perform(put("/api/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Email is already registered"));
    }

    @Test
    @WithMockUser
    void updateUserById_PasswordNotComplex() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));

        User user = new User("newUser", "newEmail@test.com", "simplepass");
        mockMvc.perform(put("/api/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Password must be at least 8 characters long and contain " +
                        "at least one uppercase letter, one lowercase letter, one digit, and one special character."));
    }

    @Test
    @WithMockUser
    void deleteUserById_Success() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));

        mockMvc.perform(delete("/api/user/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithAnonymousUser
    void deleteUserById_Unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/user/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void deleteUserById_UserNotFound() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/user/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("User not found with id: 1"));
    }

    @Test
    @WithMockUser
    void getUserById_Success() throws Exception {
        User user = new User("existingUser", "existingEmail", "password");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/user/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("existingUser"))
                .andExpect(jsonPath("$.email").value("existingEmail"));
    }

    @Test
    @WithMockUser
    void getUserById_UserNotFound() throws Exception {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/user/{id}", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("User not found with id: 1"));
    }

    @Test
    @WithAnonymousUser
    void getUserById_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/user/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getAllUsers_Success() throws Exception {
        List<User> users = List.of(
                new User("user1", "email1", "password"),
                new User("user2", "email2", "password")
        );
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    @WithMockUser
    void getAllUsers_EmptyList() throws Exception {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithAnonymousUser
    void getAllUsers_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user")
    void getProfile_Success() throws Exception {
        User user = new User("user", "email", "password");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.email").value("email"));
    }

    @Test
    @WithAnonymousUser
    void getProfile_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isForbidden());
    }

}
