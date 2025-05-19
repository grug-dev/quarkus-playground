package kkpa.application.assistants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public record PropertyData(
    // For single property responses
    Property property,

    // For multiple properties
    List<Property> properties,

    // For city count responses
    List<Counts> cities) {}
