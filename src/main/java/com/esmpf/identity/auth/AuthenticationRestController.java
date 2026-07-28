package com.esmpf.identity.auth;

import static com.esmpf.identity.auth.AuthenticationDtos.AuthenticationResponse;
import static com.esmpf.identity.auth.AuthenticationDtos.GoogleSignInCommand;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "esmpf.security.google",
        name = "enabled",
        havingValue = "true")
public class AuthenticationRestController {
    private final AuthenticationService service;

    @PostMapping("/google")
    public AuthenticationResponse signInWithGoogle(@Valid @RequestBody GoogleSignInCommand command) {
        return service.signInWithGoogle(command);
    }
}
