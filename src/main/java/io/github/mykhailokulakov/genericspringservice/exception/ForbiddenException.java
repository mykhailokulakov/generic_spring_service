package io.github.mykhailokulakov.genericspringservice.exception;

public class ForbiddenException extends DomainException {

  public ForbiddenException(ErrorCode code, Object... args) {
    super(code, args);
  }
}
