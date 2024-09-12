package com.smart.home.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.userservice.handler.BadRequestException;
import com.smart.home.userservice.handler.GlobalExceptionHandler;
import com.smart.home.userservice.model.User;
import com.smart.home.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    void testUpdateUserById_Success() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("UpdatedUser");
        user.setEmail("updated@test.com");
        user.setPassword("Password123!");

        when(userService.updateUserById(anyLong(), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("UpdatedUser")))
                .andExpect(jsonPath("$.password", is("Password123!")))
                .andExpect(jsonPath("$.email", is("updated@test.com")));

        verify(userService, times(1)).updateUserById(anyLong(), any(User.class));
    }

    @Test
    void testUpdateUserById_ValidationError() throws Exception {
        User invalidUser = new User(); // Assuming username is a required field

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUserById(anyLong(), any(User.class));
    }

    @Test
    void testDeleteUserById_Success() throws Exception {
        doNothing().when(userService).deleteUserById(anyLong());

        mockMvc.perform(delete("/api/user/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserById(anyLong());
    }

    @Test
    void testGetUserById_Success() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("TestUser");

        when(userService.getUserById(anyLong())).thenReturn(user);

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("TestUser")));

        verify(userService, times(1)).getUserById(anyLong());
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        doThrow(new BadRequestException("User not found with ID: 1")).when(userService).getUserById(anyLong());

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found with ID: 1"));

        verify(userService, times(1)).getUserById(anyLong());
    }

    @Test
    void testGetAllUsers_Success() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("TestUser");

        List<User> users = Collections.singletonList(user);

        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(1)))
                .andExpect(jsonPath("$[0].username", is("TestUser")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testUserProfile_Success() throws Exception {
        User profile = new User();
        profile.setUserId(1L);
        profile.setUsername("ProfileUser");

        when(userService.getProfile()).thenReturn(profile);

        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.username", is("ProfileUser")));

        verify(userService, times(1)).getProfile();
    }

}
