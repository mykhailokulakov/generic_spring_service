package io.github.mykhailokulakov.genericspringservice.i18n;

import static io.github.mykhailokulakov.genericspringservice.support.assertions.Assertions.assertThat;
import static io.github.mykhailokulakov.genericspringservice.support.assertions.Assertions.assertThatThrownBy;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUser;

import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.support.IntegrationTest;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

@IntegrationTest
class MessageResolutionIT {

  private static final Locale UKRAINIAN = Locale.forLanguageTag("uk");

  @LocalServerPort int port;
  @Autowired MessageSource messages;

  @ParameterizedTest
  @EnumSource(ErrorCode.class)
  void everyErrorCodeResolvesInEnglish(ErrorCode code) {
    var resolved = messages.getMessage(code.key(), null, Locale.ENGLISH);
    assertThat(resolved).isNotBlank().isNotEqualTo(code.key());
  }

  @ParameterizedTest
  @EnumSource(ErrorCode.class)
  void everyErrorCodeResolvesInUkrainian(ErrorCode code) {
    var resolved = messages.getMessage(code.key(), null, UKRAINIAN);
    assertThat(resolved).isNotBlank().isNotEqualTo(code.key());
  }

  @ParameterizedTest
  @EnumSource(ErrorCode.class)
  void ukrainianMessageDiffersFromEnglishMessage(ErrorCode code) {
    var english = messages.getMessage(code.key(), null, Locale.ENGLISH);
    var ukrainian = messages.getMessage(code.key(), null, UKRAINIAN);
    assertThat(ukrainian).isNotEqualTo(english);
  }

  @ParameterizedTest
  @EnumSource(ErrorCode.class)
  void missingLocaleFallsBackToDefault(ErrorCode code) {
    var english = messages.getMessage(code.key(), null, Locale.ENGLISH);
    var fallback = messages.getMessage(code.key(), null, Locale.JAPANESE);
    assertThat(fallback).isEqualTo(english);
  }

  @Test
  void unknownKeyThrows() {
    assertThatThrownBy(() -> messages.getMessage("error.does-not-exist", null, Locale.ENGLISH))
        .isInstanceOf(NoSuchMessageException.class);
  }

  @Test
  void nonsenseAcceptLanguageHeaderFallsBackToDefault() {
    var id = UUID.randomUUID();

    var response =
        asUser()
            .header("Accept-Language", "zz")
            .get("/api/v1/examples/" + id)
            .then()
            .extract()
            .response();

    var englishDetail =
        messages.getMessage(ErrorCode.EXAMPLE_NOT_FOUND.key(), new Object[] {id}, Locale.ENGLISH);
    var englishTitle = messages.getMessage("error.not-found.title", null, Locale.ENGLISH);

    assertThat(response)
        .hasStatus(404)
        .hasProblemJsonContentType()
        .hasCode(ErrorCode.EXAMPLE_NOT_FOUND.key())
        .hasTitle(englishTitle)
        .hasDetail(englishDetail);
  }
}
