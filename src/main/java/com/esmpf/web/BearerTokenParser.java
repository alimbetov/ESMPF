package com.esmpf.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import org.springframework.security.authentication.BadCredentialsException;

final class BearerTokenParser {

    private static final int MAX_AUTHORIZATION_LENGTH = 16 * 1024;

    String parse(HttpServletRequest request) {
        Enumeration<String> headers = request.getHeaders("Authorization");
        if (headers == null || !headers.hasMoreElements()) {
            return null;
        }
        String value = headers.nextElement();
        if (headers.hasMoreElements()) {
            throw new BadCredentialsException("Multiple Authorization headers are not allowed");
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > MAX_AUTHORIZATION_LENGTH) {
            throw new BadCredentialsException("Authorization header is too long");
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isISOControl(c)) {
                throw new BadCredentialsException("Authorization header contains control characters");
            }
        }

        int separator = value.indexOf(' ');
        if (separator <= 0 || separator == value.length() - 1) {
            throw new BadCredentialsException("Malformed bearer credentials");
        }
        String scheme = value.substring(0, separator);
        String token = value.substring(separator + 1);
        if (!"Bearer".equalsIgnoreCase(scheme)
                || token.isBlank()
                || token.indexOf(' ') >= 0
                || token.indexOf(',') >= 0) {
            throw new BadCredentialsException("Malformed bearer credentials");
        }
        return token;
    }
}
