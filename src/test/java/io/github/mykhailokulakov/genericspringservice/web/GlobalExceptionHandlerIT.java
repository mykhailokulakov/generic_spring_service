package io.github.mykhailokulakov.genericspringservice.web;

import static io.github.mykhailokulakov.genericspringservice.support.assertions.Assertions.assertThat;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asAdmin;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUser;

import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.support.IntegrationTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import io.restassured.http.ContentType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;

@IntegrationTest
@Import(DatabaseStateHelper.class)
class GlobalExceptionHandlerIT {

  private static final String PATH = "/api/v1/examples";
  private static final Locale UKRAINIAN = Locale.forLanguageTag("uk");

  @LocalServerPort int port;
  @Autowired DatabaseStateHelper db;
  @Autowired MessageSource messages;

  @BeforeEach
  void clean() {
    db.truncateAll();
  }

  private String title(String slug, Locale locale) {
    return messages.getMessage("error." + slug + ".title", null, locale);
  }

  private static Map<String, Object> validCreateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("name", "Widget");
    body.put("status", "ACTIVE");
    body.put("tags", List.of("alpha"));
    return body;
  }

  private UUID createExample() {
    return UUID.fromString(
        asAdmin()
            .contentType(ContentType.JSON)
            .body(validCreateBody())
            .post(PATH)
            .then()
            .statusCode(201)
            .extract()
            .response()
            .jsonPath()
            .getString("id"));
  }

  // --- handleNotFound (NotFoundException) ----------------------------------

  @Test
  void notFound_returns404_problemJson_codeAndLocalizedTitle() {
    var response =
        asUser()
            .header("Accept-Language", "uk")
            .get(PATH + "/" + UUID.randomUUID())
            .then()
            .extract()
            .response();

    assertThat(response)
        .hasStatus(404)
        .hasProblemJsonContentType()
        .hasCode(ErrorCode.EXAMPLE_NOT_FOUND.key())
        .hasTitle(title("not-found", UKRAINIAN));
  }

  // --- handleConflict (ConflictException → IF_MATCH_REQUIRED) --------------

  @Test
  void ifMatchRequired_returns412_problemJson_codeAndLocalizedTitle() {
    UUID id = createExample();

    var response =
        asAdmin()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "uk")
            .body(Map.of("name", "renamed"))
            .patch(PATH + "/" + id)
            .then()
            .extract()
            .response();

    assertThat(response)
        .hasStatus(412)
        .hasProblemJsonContentType()
        .hasCode(ErrorCode.IF_MATCH_REQUIRED.key())
        .hasTitle(title("precondition-failed", UKRAINIAN));
  }

  // --- handleConflict (ConflictException → OPTIMISTIC_LOCK) ----------------

  @Test
  void optimisticLockConflict_returns409_problemJson_codeAndLocalizedTitle() {
    UUID id = createExample();

    var response =
        asAdmin()
            .contentType(ContentType.JSON)
            .header("If-Match", "999")
            .header("Accept-Language", "uk")
            .body(Map.of("name", "renamed"))
            .patch(PATH + "/" + id)
            .then()
            .extract()
            .response();

    assertThat(response)
        .hasStatus(409)
        .hasProblemJsonContentType()
        .hasCode(ErrorCode.OPTIMISTIC_LOCK.key())
        .hasTitle(title("conflict", UKRAINIAN));
  }

  // --- handleAccessDenied (AccessDeniedException) --------------------------

  @Test
  void accessDenied_returns403_problemJson_codeAndLocalizedTitle() {
    var response =
        asUser()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "uk")
            .body(validCreateBody())
            .post(PATH)
            .then()
            .extract()
            .response();

    assertThat(response)
        .hasStatus(403)
        .hasProblemJsonContentType()
        .hasCode(ErrorCode.FORBIDDEN.key())
        .hasTitle(title("forbidden", UKRAINIAN));
  }

  // --- handleMethodArgumentNotValid (MethodArgumentNotValidException) ------

  @Test
  void methodArgumentNotValid_returns400_problemJson_codeAndLocalizedTitle() {
    Map<String, Object> body = validCreateBody();
    body.remove("name");

    var response =
        asAdmin()
            .contentType(ContentType.JSON)
            .header("Accept-Language", "uk")
            .body(body)
            .post(PATH)
            .then()
            .extract()
            .response();

    assertThat(response)
        .hasStatus(400)
        .hasProblemJsonContentType()
        .hasCode(ErrorCode.VALIDATION_FAILED.key())
        .hasTitle(title("validation", UKRAINIAN))
        .hasViolation("name", "NotBlank");
  }

  // --- Default-locale title sanity check -----------------------------------

  @Test
  void noAcceptLanguage_returnsDefaultEnglishTitle() {
    var response = asUser().get(PATH + "/" + UUID.randomUUID()).then().extract().response();

    assertThat(response)
        .hasStatus(404)
        .hasProblemJsonContentType()
        .hasCode(ErrorCode.EXAMPLE_NOT_FOUND.key())
        .hasTitle(title("not-found", Locale.ENGLISH));
  }
}
