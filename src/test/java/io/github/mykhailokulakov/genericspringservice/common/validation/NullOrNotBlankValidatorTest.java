package io.github.mykhailokulakov.genericspringservice.common.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NullOrNotBlankValidatorTest {

  private final NullOrNotBlankValidator validator = new NullOrNotBlankValidator();

  @Test
  void nullIsValid() {
    assertThat(validator.isValid(null, null)).isTrue();
  }

  @Test
  void nonBlankIsValid() {
    assertThat(validator.isValid("name", null)).isTrue();
  }

  @Test
  void emptyIsInvalid() {
    assertThat(validator.isValid("", null)).isFalse();
  }

  @Test
  void whitespaceOnlyIsInvalid() {
    assertThat(validator.isValid("   ", null)).isFalse();
    assertThat(validator.isValid("\t\n", null)).isFalse();
  }
}
