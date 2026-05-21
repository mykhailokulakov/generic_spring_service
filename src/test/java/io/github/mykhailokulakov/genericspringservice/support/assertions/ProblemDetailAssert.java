package io.github.mykhailokulakov.genericspringservice.support.assertions;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.AbstractAssert;

public class ProblemDetailAssert extends AbstractAssert<ProblemDetailAssert, Response> {

  private static final String PROBLEM_JSON = "application/problem+json";

  public ProblemDetailAssert(Response actual) {
    super(actual, ProblemDetailAssert.class);
  }

  public ProblemDetailAssert hasStatus(int expected) {
    isNotNull();
    int actualStatus = actual.statusCode();
    if (actualStatus != expected) {
      failWithMessage(
          "Expected HTTP status <%d> but was <%d>%n  body: %s",
          expected, actualStatus, actual.asString());
    }
    return this;
  }

  public ProblemDetailAssert hasProblemJsonContentType() {
    return hasContentType(PROBLEM_JSON);
  }

  public ProblemDetailAssert hasContentType(String expected) {
    isNotNull();
    String actualContentType = actual.getContentType();
    if (actualContentType == null
        || !actualContentType.toLowerCase().startsWith(expected.toLowerCase())) {
      failWithMessage(
          "Expected Content-Type to start with <%s> but was <%s>", expected, actualContentType);
    }
    return this;
  }

  public ProblemDetailAssert hasCode(String expected) {
    return assertStringField("code", expected);
  }

  public ProblemDetailAssert hasTitle(String expected) {
    return assertStringField("title", expected);
  }

  public ProblemDetailAssert hasTitleMatching(String regex) {
    isNotNull();
    String actualTitle = jsonPath().getString("title");
    if (actualTitle == null || !actualTitle.matches(regex)) {
      failWithMessage(
          "Expected $.title to match <%s> but was <%s>%n  body: %s",
          regex, actualTitle, actual.asString());
    }
    return this;
  }

  public ProblemDetailAssert hasDetail(String expected) {
    return assertStringField("detail", expected);
  }

  public ProblemDetailAssert hasType(URI expected) {
    isNotNull();
    String actualType = jsonPath().getString("type");
    if (actualType == null || !URI.create(actualType).equals(expected)) {
      failWithMessage(
          "Expected $.type to be <%s> but was <%s>%n  body: %s",
          expected, actualType, actual.asString());
    }
    return this;
  }

  public ProblemDetailAssert hasViolation(String field, String code) {
    isNotNull();
    List<Map<String, Object>> violations = violations();
    boolean found =
        violations.stream()
            .anyMatch(v -> field.equals(v.get("field")) && code.equals(v.get("code")));
    if (!found) {
      failWithMessage(
          "Expected a violation with field=<%s> and code=<%s> but violations were:%n  %s",
          field, code, violations);
    }
    return this;
  }

  public ProblemDetailAssert hasViolationCount(int expected) {
    isNotNull();
    List<Map<String, Object>> violations = violations();
    if (violations.size() != expected) {
      failWithMessage(
          "Expected <%d> violations but found <%d>:%n  %s",
          expected, violations.size(), violations);
    }
    return this;
  }

  private ProblemDetailAssert assertStringField(String field, String expected) {
    isNotNull();
    String actualValue = jsonPath().getString(field);
    if (actualValue == null || !actualValue.equals(expected)) {
      failWithMessage(
          "Expected $.%s to be <%s> but was <%s>%n  body: %s",
          field, expected, actualValue, actual.asString());
    }
    return this;
  }

  private JsonPath jsonPath() {
    return actual.jsonPath();
  }

  private List<Map<String, Object>> violations() {
    List<Map<String, Object>> violations = jsonPath().getList("violations");
    return violations == null ? List.of() : violations;
  }
}
