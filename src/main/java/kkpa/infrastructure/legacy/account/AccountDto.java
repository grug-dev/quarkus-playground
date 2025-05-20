package kkpa.infrastructure.legacy.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Account data from legacy system */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccountDto(Long id, String firstName, String lastName, String email) {}
