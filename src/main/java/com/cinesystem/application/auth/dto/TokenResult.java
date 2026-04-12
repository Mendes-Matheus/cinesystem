package com.cinesystem.application.auth.dto;

public record TokenResult(String accessToken, String tokenType, Long expiresIn) {
    public TokenResult {
        if (tokenType == null) {
            tokenType = "Bearer";
        }
    }
    
    public static TokenResult of(String accessToken, Long expiresIn) {
        return new TokenResult(accessToken, "Bearer", expiresIn);
    }
}
