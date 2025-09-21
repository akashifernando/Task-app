package com.project.TaskApp.service;

import com.project.TaskApp.dto.Response;
import com.project.TaskApp.dto.UserRequest;
import com.project.TaskApp.entity.AppUser;
import com.project.TaskApp.enums.Role;
import com.project.TaskApp.exceptions.BadRequestException;
import com.project.TaskApp.exceptions.NotFoundException;
import com.project.TaskApp.repo.UserRepository;
import com.project.TaskApp.security.JwtUtils;
import com.project.TaskApp.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;   // ✅ now fields
    private JwtUtils jwtUtils;                 // ✅ now fields
    private UserServiceImpl userService;

    @BeforeEach
    void setup() {
        userRepository  = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);   // ✅ field instance
        jwtUtils        = mock(JwtUtils.class);          // ✅ field instance
        userService     = new UserServiceImpl(userRepository, passwordEncoder, jwtUtils);

        when(passwordEncoder.encode("secret")).thenReturn("ENC(secret)");
    }

    // -------- Register tests --------
    @Test
    void signUp_success_whenNewUser() {
        UserRequest req = new UserRequest();
        req.setUsername("alice");
        req.setPassword("secret");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        Response<?> resp = userService.signUp(req);

        assertEquals(201, resp.getStatusCode());
        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        assertEquals("alice", captor.getValue().getUsername());
    }

    @Test
    void signUp_fail_whenUserExists() {
        UserRequest req = new UserRequest();
        req.setUsername("bob");
        req.setPassword("x");

        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(new AppUser()));

        assertThrows(BadRequestException.class, () -> userService.signUp(req));
    }

    // -------- Login tests --------
    @Test
    void login_success_whenCredentialsCorrect() {
        UserRequest req = new UserRequest();
        req.setUsername("alice");
        req.setPassword("secret");

        AppUser user = AppUser.builder()
                .username("alice")
                .password("ENC(secret)")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "ENC(secret)")).thenReturn(true);
        when(jwtUtils.generateToken("alice")).thenReturn("jwt-123");

        Response<?> resp = userService.login(req);

        assertEquals(200, resp.getStatusCode());
        assertEquals("login successful", resp.getMessage());
        assertEquals("jwt-123", resp.getData());
    }

    @Test
    void login_fail_whenUserNotFound() {
        UserRequest req = new UserRequest();
        req.setUsername("ghost");
        req.setPassword("x");

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.login(req));
    }

    @Test
    void login_fail_whenPasswordWrong() {
        UserRequest req = new UserRequest();
        req.setUsername("alice");
        req.setPassword("wrong");

        AppUser user = AppUser.builder()
                .username("alice")
                .password("ENC(secret)")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "ENC(secret)")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.login(req));
    }
}
