package io.github.mykhailokulakov.genericspringservice.exception;

public final class ConflictException extends DomainException {

  public ConflictException(ErrorCode code, Object... args) {
    super(code, args);
  }
}
