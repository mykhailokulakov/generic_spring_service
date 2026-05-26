package io.github.mykhailokulakov.genericspringservice.exception;

public enum ErrorCode {
  EXAMPLE_NOT_FOUND("error.example.not-found"),
  PARENT_NOT_FOUND("error.parent.not-found"),
  CHILD_NOT_FOUND("error.child.not-found"),
  LEFT_NOT_FOUND("error.left.not-found"),
  RIGHT_NOT_FOUND("error.right.not-found"),
  OWNER_NOT_FOUND("error.owner.not-found"),
  OPTIMISTIC_LOCK("error.optimistic-lock"),
  VALIDATION_FAILED("error.validation.failed"),
  FORBIDDEN("error.forbidden"),
  IF_MATCH_REQUIRED("error.if-match.required");

  private final String key;

  ErrorCode(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }
}
