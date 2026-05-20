package io.github.mykhailokulakov.genericspringservice.exception;

public final class ValidationException extends DomainException {

  public ValidationException(ErrorCode code, Object... args) {
    super(code, args);
  }
}
