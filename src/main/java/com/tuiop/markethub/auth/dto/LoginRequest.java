package com.tuiop.markethub.security.auth.dto;


public record LoginRequest(
        String email,
        String password
) {

}
