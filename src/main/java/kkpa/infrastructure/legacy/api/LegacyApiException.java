package kkpa.infrastructure.legacy.api;

/** Exception thrown for legacy API communication issues */
public class LegacyApiException extends RuntimeException {

  public LegacyApiException(String message) {
    super(message);
  }

  public LegacyApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
