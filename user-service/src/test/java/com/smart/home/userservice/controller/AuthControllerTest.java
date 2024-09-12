package com.smart.home.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.userservice.model.LoginDTO;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testLogin_Success() throws Exception {
        LoginDTO loginDTO = new LoginDTO("testUser", "password123");
        String token = "dummyToken";

        when(userService.login(any(LoginDTO.class))).thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string(token));

        verify(userService, times(1)).login(loginDTO);
    }


    @Test
    void testRegister_Success() throws Exception {
        User user = new User();
        user.setUsername("testUser");
        user.setEmail("testUser@test.com");
        user.setPassword("Password123!");

        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("testUser")))
                .andExpect(jsonPath("$.email", is("testUser@test.com")));

        verify(userService, times(1)).createUser(user);
    }

    @Test
    void testRegister_Failure_ValidationErrors() throws Exception {
        User user = new User();
        user.setUsername(""); // Invalid username
        user.setEmail("invalidEmail"); // Invalid email
        user.setPassword("weak"); // Invalid password

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[*]", containsInAnyOrder(
                        "username: Username must be between 1 and 50 characters",
                        "password: Password must be between 8 and 100 characters",
                        "email: Email local part allows: - Numeric values from 0 to 9. - Uppercase and lowercase letters from a to z. - Underscore “_”, hyphen “-“, and dot “.” - Dot isn’t allowed at the start and end of the local part. - Consecutive dots aren’t allowed. - A maximum of 64 characters are allowed. Email domain part allows: - Numeric values from 0 to 9. - Uppercase and lowercase letters from a to z. - Hyphen “-” and dot “.” aren’t allowed at the start and end of the domain part. - Consecutive dots aren’t allowed.",
                        "username: Username must not be blank")));

        verify(userService, times(0)).createUser(any(User.class));
    }

}
