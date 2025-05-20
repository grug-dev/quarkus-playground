package kkpa.infrastructure.legacy.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Property data from legacy system */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PropertyDto(
    Long id,
    String propertyName,
    String propertyType,
    String address,
    String city,
    String state,
    String zipCode,
    Double price,
    Integer bedrooms,
    Integer bathrooms,
    Integer squareFeet,
    Boolean isActive) {}
