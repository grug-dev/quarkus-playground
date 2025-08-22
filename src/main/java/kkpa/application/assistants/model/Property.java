package kkpa.application.assistants.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record Property(
    String id,
    String type,
    String city,
    // Other common property fields (can be null if not provided)
    String price,
    String size,
    String bedrooms) {}
