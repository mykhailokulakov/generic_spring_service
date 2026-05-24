package io.github.mykhailokulakov.genericspringservice.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private static final String PROBLEM_TYPE_BASE = "https://generic-spring-service/problems/";

  private final MessageSource messages;

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFound(NotFoundException ex, Locale locale) {
    return problem(HttpStatus.NOT_FOUND, ex, locale, "not-found");
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail handleConflict(ConflictException ex, Locale locale) {
    if (ErrorCode.IF_MATCH_REQUIRED.key().equals(ex.getMessageKey())) {
      return problem(HttpStatus.PRECONDITION_FAILED, ex, locale, "precondition-failed");
    }
    return problem(HttpStatus.CONFLICT, ex, locale, "conflict");
  }

  @ExceptionHandler(ForbiddenException.class)
  public ProblemDetail handleForbidden(ForbiddenException ex, Locale locale) {
    return problem(HttpStatus.FORBIDDEN, ex, locale, "forbidden");
  }

  @ExceptionHandler(ValidationException.class)
  public ProblemDetail handleDomainValidation(ValidationException ex, Locale locale) {
    return problem(HttpStatus.BAD_REQUEST, ex, locale, "validation");
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    var locale = LocaleContextHolder.getLocale();
    var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setType(URI.create(PROBLEM_TYPE_BASE + "validation"));
    pd.setTitle(messages.getMessage("error.validation.title", null, locale));
    pd.setDetail(messages.getMessage(ErrorCode.VALIDATION_FAILED.key(), null, locale));
    pd.setProperty("code", ErrorCode.VALIDATION_FAILED.key());
    var violations =
        ex.getBindingResult().getAllErrors().stream()
            .map(
                error ->
                    Map.<String, Object>of(
                        "field",
                        (error instanceof FieldError fe) ? fe.getField() : error.getObjectName(),
                        "code",
                        error.getCode() == null ? "" : error.getCode(),
                        "message",
                        messages.getMessage(error, locale)))
            .toList();
    pd.setProperty("violations", violations);
    return handleExceptionInternal(ex, pd, headers, HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, Locale locale) {
    var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    pd.setType(URI.create(PROBLEM_TYPE_BASE + "validation"));
    pd.setTitle(messages.getMessage("error.validation.title", null, locale));
    pd.setDetail(messages.getMessage(ErrorCode.VALIDATION_FAILED.key(), null, locale));
    pd.setProperty("code", ErrorCode.VALIDATION_FAILED.key());
    var violations =
        ex.getConstraintViolations().stream()
            .map(
                cv ->
                    Map.<String, Object>of(
                        "field", pathLeaf(cv),
                        "code", constraintCode(cv),
                        "message", cv.getMessage()))
            .toList();
    pd.setProperty("violations", violations);
    return pd;
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ProblemDetail handleOptimisticLocking(
      OptimisticLockingFailureException ex, Locale locale) {
    var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
    pd.setType(URI.create(PROBLEM_TYPE_BASE + "conflict"));
    pd.setTitle(messages.getMessage("error.conflict.title", null, locale));
    pd.setDetail(messages.getMessage(ErrorCode.OPTIMISTIC_LOCK.key(), null, locale));
    pd.setProperty("code", ErrorCode.OPTIMISTIC_LOCK.key());
    return pd;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex, Locale locale) {
    var pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    pd.setType(URI.create(PROBLEM_TYPE_BASE + "forbidden"));
    pd.setTitle(messages.getMessage("error.forbidden.title", null, locale));
    pd.setDetail(messages.getMessage(ErrorCode.FORBIDDEN.key(), null, locale));
    pd.setProperty("code", ErrorCode.FORBIDDEN.key());
    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex, Locale locale) {
    log.error("Unhandled exception", ex);
    var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    pd.setType(URI.create(PROBLEM_TYPE_BASE + "internal"));
    pd.setTitle(messages.getMessage("error.internal.title", null, locale));
    pd.setDetail(messages.getMessage("error.internal", null, locale));
    pd.setProperty("code", "error.internal");
    return pd;
  }

  private ProblemDetail problem(HttpStatus status, DomainException ex, Locale locale, String slug) {
    var pd = ProblemDetail.forStatus(status);
    pd.setType(URI.create(PROBLEM_TYPE_BASE + slug));
    pd.setTitle(messages.getMessage("error." + slug + ".title", null, locale));
    pd.setDetail(messages.getMessage(ex.getMessageKey(), ex.getArgs(), locale));
    pd.setProperty("code", ex.getMessageKey());
    return pd;
  }

  private static String pathLeaf(ConstraintViolation<?> cv) {
    String path = cv.getPropertyPath() == null ? "" : cv.getPropertyPath().toString();
    int dot = path.lastIndexOf('.');
    return dot >= 0 ? path.substring(dot + 1) : path;
  }

  private static String constraintCode(ConstraintViolation<?> cv) {
    return cv.getConstraintDescriptor() == null
            || cv.getConstraintDescriptor().getAnnotation() == null
        ? ""
        : cv.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
  }
}
