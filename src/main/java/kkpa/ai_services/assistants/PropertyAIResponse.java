package kkpa.ai_services.assistants;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public record PropertyAIResponse(
    // Natural language explanation/response
    String explanation,

    // Structured data - could be null if only explanation is provided
    PropertyData data) {}
