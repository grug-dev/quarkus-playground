package kkpa.infrastructure.legacy.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResponseDto(
    @JsonProperty("access_token") String access_token,
    @JsonProperty("token_type") String token_type,
    @JsonProperty("refresh_token") String refresh_token,
    @JsonProperty("expires_in") int expires_in,
    @JsonProperty("scope") String scope) {}
