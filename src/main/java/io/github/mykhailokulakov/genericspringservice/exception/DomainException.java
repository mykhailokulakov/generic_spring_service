package io.github.mykhailokulakov.genericspringservice.exception;

public abstract sealed class DomainException extends RuntimeException
    permits NotFoundException, ConflictException, ValidationException, ForbiddenException {

  private final String messageKey;
  private final Object[] args;

  protected DomainException(ErrorCode code, Object... args) {
    super(code.key());
    this.messageKey = code.key();
    this.args = args == null ? new Object[0] : args;
  }

  public String getMessageKey() {
    return messageKey;
  }

  public Object[] getArgs() {
    return args.clone();
  }
}
