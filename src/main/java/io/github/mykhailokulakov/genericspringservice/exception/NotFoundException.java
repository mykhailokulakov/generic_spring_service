package io.github.mykhailokulakov.genericspringservice.exception;

public class NotFoundException extends DomainException {

  public NotFoundException(ErrorCode code, Object... args) {
    super(code, args);
  }
}
