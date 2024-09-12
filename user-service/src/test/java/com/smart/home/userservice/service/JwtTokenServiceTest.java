package com.smart.home.userservice.service;

import com.smart.home.userservice.model.Role;
import com.smart.home.userservice.model.RoleType;
import com.smart.home.userservice.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService= new JwtTokenService("mySuperSecretKeyThatShouldBeLongEnoughForTheTest123!", 1000L * 60 * 60);
    }

    @Test
    void generateToken_Success() {
        String username = "testUser";
        Set<com.smart.home.userservice.model.Role> roles = new HashSet<>();
        roles.add(new Role(RoleType.USER));

        String token = jwtTokenService.generateToken(username, roles);

        assertNotNull(token);
    }

    @Test
    void extractUsernameFromToken_Success() {
        String username = "testUser";
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(RoleType.USER));

        String token = jwtTokenService.generateToken(username, roles);
        String extractedUsername = jwtTokenService.extractUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void extractUsernameFromToken_TokenExpired() throws InterruptedException {
        jwtTokenService = new JwtTokenService("mySuperSecretKeyThatShouldBeLongEnoughForTheTest123!", 1000L);

        String username = "testUser";
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(RoleType.USER));

        String token = jwtTokenService.generateToken(username, roles);

        Thread.sleep(1500L); // Wait for token to expire

        String extractedUsername = jwtTokenService.extractUsernameFromToken(token);


        assertNull (extractedUsername);
    }

    @Test
    void isTokenExpired_False() {
        String username = "testUser";
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(RoleType.USER));

        String token = jwtTokenService.generateToken(username, roles);
        boolean isExpired = jwtTokenService.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void isTokenExpired_True() throws InterruptedException {
        jwtTokenService = new JwtTokenService("mySuperSecretKeyThatShouldBeLongEnoughForTheTest123!", 1000L);

        String username = "testUser";
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(RoleType.USER));

        String token = jwtTokenService.generateToken(username, roles);

        Thread.sleep(1500L); // Wait for token to expire

        boolean isExpired = jwtTokenService.isTokenExpired(token);

        assertTrue(isExpired);
    }

    @Test
    void getClaims_Success() {
        String username = "testUser";
        Set<Role> roles = new HashSet<>();
        roles.add(new Role(RoleType.USER));

        String token = jwtTokenService.generateToken(username, roles);
        String claims = jwtTokenService.getClaims(token, Claims::getSubject);

        assertEquals(username, claims);
    }

}