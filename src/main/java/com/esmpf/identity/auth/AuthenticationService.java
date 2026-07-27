package com.esmpf.identity.auth;

import static com.esmpf.identity.auth.AuthenticationDtos.AuthenticationResponse;
import static com.esmpf.identity.auth.AuthenticationDtos.GoogleSignInCommand;

/**
 * Application contract for future authentication controllers.
 */
public interface AuthenticationService {

    AuthenticationResponse signInWithGoogle(GoogleSignInCommand command);
}
