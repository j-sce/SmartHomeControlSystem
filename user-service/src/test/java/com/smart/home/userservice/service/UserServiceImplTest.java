package com.smart.home.userservice.service;

import com.smart.home.userservice.handler.BadRequestException;
import com.smart.home.userservice.model.LoginDTO;
import com.smart.home.userservice.model.TokenValidationRequest;
import com.smart.home.userservice.model.User;
import com.smart.home.userservice.repository.UserRepository;
import com.smart.home.userservice.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuthenticationProvider authenticationProvider;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void login_Success() {
        LoginDTO loginDTO = new LoginDTO("testUser", "P@ssword1");
        User user = new User();
        user.setUsername("testUser");
        user.setRoles(Set.of());

        Authentication authentication = mock(Authentication.class);
        when(authenticationProvider.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(jwtTokenService.generateToken(anyString(), anySet())).thenReturn("token");

        String token = userService.login(loginDTO);

        assertEquals("token", token);
        verify(authenticationProvider, times(1)).authenticate(any(Authentication.class));
        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(jwtTokenService, times(1)).generateToken(anyString(), anySet());
    }

    @Test
    void login_InvalidCredentials_ThrowsBadCredentialsException() {
        LoginDTO loginDTO = new LoginDTO("invalidUser", "wrongPassword");

        when(authenticationProvider.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Invalid username or password."));

        assertThrows(BadCredentialsException.class, () -> userService.login(loginDTO));

        verify(authenticationProvider, times(1)).authenticate(any(Authentication.class));
        verify(jwtTokenService, times(0)).generateToken(anyString(), anySet());
        verify(userRepository, times(0)).findByUsername(anyString());
    }

    @Test
    void createUser_Success() {
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("P@ssword1");
        user.setEmail("user@test.com");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser(user);

        assertNotNull(result);
        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void createUser_UsernameAlreadyTaken_ThrowsBadRequestException() {
        User user = new User();
        user.setUsername("existingUser");
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.createUser(user));
        verify(userRepository, times(1)).findByUsername(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyRegistered_ThrowsBadRequestException() {
        User user = new User();
        user.setEmail("existing@test.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> userService.createUser(user));
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void updateUserById_Success() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setUsername("oldUser");
        existingUser.setEmail("oldemail@test.com");
        existingUser.setPassword("encodedPassword");

        User userUpdate = new User();
        userUpdate.setUsername("newUser");
        userUpdate.setEmail("newemail@test.com");
        userUpdate.setPassword("newP@ssword1");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUserById(userId, userUpdate);

        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
        assertEquals("newemail@test.com", result.getEmail());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByUsername(userUpdate.getUsername());
        verify(userRepository, times(1)).findByEmail(userUpdate.getEmail());
        verify(passwordEncoder, times(2)).encode(anyString());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUserById_UserNotFound_ThrowsBadRequestException() {
        Long userId = 1L;
        User userUpdate = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userService.updateUserById(userId, userUpdate));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void updateUserById_InvalidPassword_ThrowsBadRequestException() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setUsername("existingUser");
        existingUser.setPassword("encodedOldPassword");

        User userUpdate = new User();
        userUpdate.setUsername("newUser");
        userUpdate.setEmail("newemail@test.com");
        userUpdate.setPassword("simple");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedSimplePassword");

        assertThrows(BadRequestException.class, () -> userService.updateUserById(userId, userUpdate));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).findByUsername(userUpdate.getUsername());
        verify(userRepository, times(1)).findByEmail(userUpdate.getEmail());
        verify(passwordEncoder, times(1)).encode(anyString());
        verify(userRepository, times(0)).save(any(User.class));
    }

    @Test
    void deleteUserById_Success() {
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUserById(userId);

        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void deleteUserById_UserNotFound_ThrowsBadRequestException() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userService.deleteUserById(userId));
    }

    @Test
    void getUserById_Success() {
        Long userId = 1L;
        User user = new User();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUserById_UserNotFound_ThrowsBadRequestException() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> userService.getUserById(userId));
    }

    @Test
    void getAllUsers_Success() {
        List<User> users = new ArrayList<>();
        users.add(new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsers_EmptyList() {
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getProfile_Success() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testUser");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(mock(Authentication.class));
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);

        User user = new User();
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        User result = userService.getProfile();

        assertNotNull(result);
        verify(userDetails, times(1)).getUsername();
        verify(userRepository, times(1)).findByUsername("testUser");
    }

    @Test
    void validateToken_Success() {
        TokenValidationRequest tokenValidationRequest = new TokenValidationRequest();
        tokenValidationRequest.setToken("Bearer test-token");

        when(jwtTokenService.validateToken(anyString())).thenReturn(true);

        Boolean isValid = userService.validateToken(tokenValidationRequest);

        assertTrue(isValid);
        verify(jwtTokenService, times(1)).validateToken(anyString());
    }

    @Test
    void validateToken_InvalidToken() {
        TokenValidationRequest tokenValidationRequest = new TokenValidationRequest();
        tokenValidationRequest.setToken("Bearer invalidToken");

        when(jwtTokenService.validateToken(anyString())).thenReturn(false);

        Boolean isValid = userService.validateToken(tokenValidationRequest);

        assertFalse(isValid);
        verify(jwtTokenService, times(1)).validateToken(anyString());
    }

}
