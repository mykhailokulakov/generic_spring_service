package io.github.mykhailokulakov.genericspringservice.web.annotation;

public final class ApiResponseDescriptions {

  public static final String UNAUTHENTICATED = "Unauthenticated";
  public static final String FORBIDDEN = "Forbidden — insufficient role";
  public static final String NOT_FOUND = "Resource not found";
  public static final String VALIDATION_FAILED = "Validation failed";
  public static final String CONFLICT = "Optimistic lock conflict";
  public static final String IF_MATCH_PRECONDITION = "If-Match header missing or malformed";

  private ApiResponseDescriptions() {}
}
