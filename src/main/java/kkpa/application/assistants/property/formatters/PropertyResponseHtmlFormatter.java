package kkpa.application.assistants.property.formatters;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import kkpa.application.assistants.Counts;
import kkpa.application.assistants.Property;
import kkpa.application.assistants.PropertyAIResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Formats PropertyAIResponse objects into pretty HTML for HTMX responses. Follows Single
 * Responsibility Principle by handling only response formatting.
 */
public class PropertyResponseHtmlFormatter {

  private Logger log = LoggerFactory.getLogger(PropertyResponseHtmlFormatter.class);

  /**
   * Converts a PropertyAIResponse to an HTML string suitable for HTMX updates.
   *
   * @param response The AI response to format
   * @return Formatted HTML string
   */
  public String formatToHtml(PropertyAIResponse response) {
    log.info("Formatting PropertyAIResponse to HTML");
    StringBuilder html = new StringBuilder();

    // Start message container
    html.append("<div class=\"message ai-message\">");
    html.append("<div class=\"message-content\">");

    // Add explanation text
    html.append("<div class=\"explanation\">").append(response.explanation()).append("</div>");

    // Add structured data if available
    if (response.data() != null) {
      html.append("<div class=\"structured-data\">");

      // Handle single property
      if (response.data().property() != null) {
        html.append(formatSingleProperty(response.data().property()));
      }

      // Handle multiple properties
      if (response.data().properties() != null && !response.data().properties().isEmpty()) {
        html.append(formatMultipleProperties(response.data().properties()));
      }

      // Handle city counts
      if (response.data().cities() != null && !response.data().cities().isEmpty()) {
        html.append(formatCityCounts(response.data().cities()));
      }

      html.append("</div>");
    }

    html.append("</div>"); // Close message-content

    // Add timestamp
    html.append("<div class=\"message-time\">")
        .append(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
        .append("</div>");

    // Close container
    html.append("</div>");

    return html.toString();
  }

  private String formatSingleProperty(Property property) {
    StringBuilder html = new StringBuilder();
    html.append("<div class=\"property-card\">");
    html.append("<h3>Property Details</h3>");
    html.append("<ul class=\"property-details\">");

    // Always include these fields
    html.append("<li><strong>ID:</strong> ").append(property.id()).append("</li>");
    if (property.type() != null) {
      html.append("<li><strong>Type:</strong> ").append(property.type()).append("</li>");
    }
    if (property.city() != null) {
      html.append("<li><strong>City:</strong> ").append(property.city()).append("</li>");
    }

    // Include optional fields if present
    if (property.price() != null) {
      html.append("<li><strong>Price:</strong> ").append(property.price()).append("</li>");
    }
    if (property.size() != null) {
      html.append("<li><strong>Size:</strong> ").append(property.size()).append("</li>");
    }
    if (property.bedrooms() != null) {
      html.append("<li><strong>Bedrooms:</strong> ").append(property.bedrooms()).append("</li>");
    }

    html.append("</ul>");
    html.append("</div>");
    return html.toString();
  }

  private String formatMultipleProperties(java.util.List<Property> properties) {
    StringBuilder html = new StringBuilder();
    html.append("<div class=\"properties-container\">");
    html.append("<h3>Properties Found (").append(properties.size()).append(")</h3>");
    html.append("<ul class=\"properties-list\">");

    for (Property property : properties) {
      html.append("<li class=\"property-item\">");
      html.append("<div><strong>ID:</strong> ").append(property.id()).append("</div>");

      if (property.type() != null) {
        html.append("<div><strong>Type:</strong> ").append(property.type()).append("</div>");
      }

      if (property.city() != null) {
        html.append("<div><strong>City:</strong> ").append(property.city()).append("</div>");
      }

      // Include additional property details if available
      if (property.price() != null || property.size() != null || property.bedrooms() != null) {
        html.append("<div class=\"additional-details\">");

        if (property.price() != null) {
          html.append("<span><strong>Price:</strong> ").append(property.price()).append("</span> ");
        }

        if (property.size() != null) {
          html.append("<span><strong>Size:</strong> ").append(property.size()).append("</span> ");
        }

        if (property.bedrooms() != null) {
          html.append("<span><strong>Bedrooms:</strong> ")
              .append(property.bedrooms())
              .append("</span>");
        }

        html.append("</div>");
      }

      html.append("</li>");
    }

    html.append("</ul>");
    html.append("</div>");
    return html.toString();
  }

  private String formatCityCounts(java.util.List<Counts> cities) {
    StringBuilder html = new StringBuilder();
    html.append("<div class=\"city-statistics\">");
    html.append("<h3>City Statistics</h3>");
    html.append("<ul class=\"city-counts\">");

    for (Counts city : cities) {
      html.append("<li><strong>")
          .append(city.name())
          .append(":</strong> ")
          .append(city.count())
          .append(" properties</li>");
    }

    html.append("</ul>");
    html.append("</div>");
    return html.toString();
  }
}
