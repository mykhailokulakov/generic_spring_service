package io.github.mykhailokulakov.genericspringservice.support.assertions;

import io.restassured.response.Response;

/**
 * Drop-in replacement for {@link org.assertj.core.api.Assertions} that adds an {@code assertThat}
 * overload for RestAssured {@link Response}. Tests can {@code import static ...Assertions.*} and
 * have both the standard AssertJ overloads and the {@link ProblemDetailAssert} entry point
 * available from a single import. Extends AssertJ's {@code Assertions} so every inherited static
 * {@code assertThat} overload remains accessible through this class — the documented AssertJ
 * pattern for custom-assertion entry points.
 */
public final class Assertions extends org.assertj.core.api.Assertions {

  private Assertions() {}

  public static ProblemDetailAssert assertThat(Response actual) {
    return new ProblemDetailAssert(actual);
  }
}
