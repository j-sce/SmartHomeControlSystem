package com.smart.home.userservice.service;

import com.smart.home.userservice.handler.BadRequestException;
import com.smart.home.userservice.model.LoginDTO;
import com.smart.home.userservice.model.TokenValidationRequest;
import com.smart.home.userservice.model.User;
import com.smart.home.userservice.repository.UserRepository;
import com.smart.home.userservice.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationProvider authenticationProvider;

    @Override
    public String login(LoginDTO loginDTO) {
        Authentication authentication = authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password."));
        return jwtTokenService.generateToken(user.getUsername(), user.getRoles());
    }

    @Override
    public User createUser(User user) {
        log.debug("Checking if username is unique: {}", user.getUsername());
        checkUniqueUsername(user.getUsername());

        log.debug("Checking if email is already registered: {}", user.getEmail());
        checkUniqueEmail(user.getEmail());

        log.debug("Checking if password is complex: {}", user.getPassword());
        checkIfPasswordIsComplex(user.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        log.info("User created: {}", user);
        return userRepository.save(user);
    }

    @Override
    public User updateUserById(Long userId, User userUpdate) {
        log.debug("Updating user with id: {}", userId);

        log.debug("Checking if user exists with id: {}", userId);
        User existingUser = getUserById(userId);

        log.debug("Checking username: {}", userUpdate.getUsername());
        if (!userUpdate.getUsername().equals(existingUser.getUsername())) {
            checkUniqueUsername(userUpdate.getUsername());
            existingUser.setUsername(userUpdate.getUsername());
        }

        log.debug("Checking email: {}", userUpdate.getEmail());
        if (!userUpdate.getEmail().equals(existingUser.getEmail())) {
            checkUniqueEmail(userUpdate.getEmail());
            existingUser.setEmail(userUpdate.getEmail());
        }

        log.debug("Checking password: {}", userUpdate.getPassword());
        if (!passwordEncoder.encode(userUpdate.getPassword()).equals(existingUser.getPassword())) {
            checkIfPasswordIsComplex(userUpdate.getPassword());
            existingUser.setPassword(passwordEncoder.encode(userUpdate.getPassword()));
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        log.info("User updated: {}", existingUser);
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUserById(Long userId) {
        log.debug("Deleting user with id: {}", userId);
        User user = getUserById(userId);
        userRepository.delete(user);
        log.info("User with id {} deleted successfully.", userId);
    }

    @Override
    public User getUserById(Long userId) {
        log.debug("Getting user with id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found with id: " + userId));
        log.info("User with id {} is {}", userId, user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        log.debug("Getting list of all users.");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.warn("Users list is empty.");
        }

        return users;
    }

    @Override
    public User getProfile() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found."));
    }

    @Override
    public Boolean validateToken(TokenValidationRequest request) {
        log.debug("Validating token.");

        String username;
        try {
            username = jwtTokenService.extractUsernameFromToken(request.getToken());
        } catch (IllegalArgumentException e){
            log.warn("Token does not contain a valid username or is expired.");
            return false;
        }

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            log.warn("User with username {} not found.", username);
            return false;
        }

        boolean isValid = jwtTokenService.validateToken(request.getToken());
        if (!isValid) {
            log.warn("Token is not valid.");
            return false;
        }
        log.info("Token validated successfully.");
        return true;
    }


    private void checkUniqueUsername(String username) {
        Optional<User> userWithUsername = userRepository.findByUsername(username);
        if (userWithUsername.isPresent()) {
            throw new BadRequestException("Username is already taken");
        }
    }

    private void checkUniqueEmail(String email) {
        Optional<User> userWithEmail = userRepository.findByEmail(email);
        if (userWithEmail.isPresent()) {
            throw new BadRequestException("Email is already registered");
        }
    }

    private void checkIfPasswordIsComplex(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        if (!password.matches(passwordRegex)) {
            throw new BadRequestException("Password must be at least 8 characters long and contain " +
                    "at least one uppercase letter, one lowercase letter, one digit, and one special character.");
        }
    }

}
