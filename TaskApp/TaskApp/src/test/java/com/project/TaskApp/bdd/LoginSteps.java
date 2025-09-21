package com.project.TaskApp.bdd;

import com.project.TaskApp.dto.Response;
import com.project.TaskApp.dto.UserRequest;
import com.project.TaskApp.entity.AppUser;
import com.project.TaskApp.enums.Role;
import com.project.TaskApp.exceptions.BadRequestException;
import com.project.TaskApp.repo.UserRepository;
import com.project.TaskApp.security.JwtUtils;
import com.project.TaskApp.service.impl.UserServiceImpl;
import io.cucumber.java.en.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginSteps {

    private final UserRepository repo = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtUtils jwt = mock(JwtUtils.class);
    private final UserServiceImpl service = new UserServiceImpl(repo, encoder, jwt);

    private Response<?> response;   // success path
    private Exception captured;     // error path

    @Given("a user {string} exists with password {string}")
    public void aUserExistsWithPassword(String username, String password) {
        AppUser user = AppUser.builder()
                .username(username)
                .password("ENC(" + password + ")")
                .role(Role.USER)
                .build();

        when(repo.findByUsername(username)).thenReturn(Optional.of(user));

        // For the CORRECT password only, say it matches:
        when(encoder.matches(password, "ENC(" + password + ")")).thenReturn(true);

        // Token to return on success
        when(jwt.generateToken(username)).thenReturn("jwt-demo");
    }

    @When("I login with username {string} and password {string}")
    public void iLoginWithUsernameAndPassword(String username, String password) {
        try {
            UserRequest req = new UserRequest();
            req.setUsername(username);
            req.setPassword(password);
            response = service.login(req);   // may throw for wrong password
        } catch (Exception ex) {
            captured = ex;                   // keep it for the Then assertion
        }
    }

    @Then("I should receive a token")
    public void iShouldReceiveAToken() {
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("jwt-demo", response.getData());
    }

    @Then("I should see a bad request error")
    public void iShouldSeeABadRequestError() {
        assertNotNull(captured, "Expected an error but none was thrown");
        assertTrue(captured instanceof BadRequestException,
                "Expected BadRequestException but got: " + captured.getClass());
    }
}
