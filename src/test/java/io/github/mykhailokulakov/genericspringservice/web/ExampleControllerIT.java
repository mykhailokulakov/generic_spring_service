package io.github.mykhailokulakov.genericspringservice.web;

import static io.github.mykhailokulakov.genericspringservice.support.assertions.Assertions.assertThat;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asAdmin;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUser;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudControllerTestContract;
import io.restassured.http.ContentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class ExampleControllerIT extends AbstractCrudControllerTestContract<ExampleEntity> {

  @Override
  protected String path() {
    return "/api/v1/examples";
  }

  @Override
  protected Class<ExampleEntity> entityClass() {
    return ExampleEntity.class;
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.EXAMPLE_NOT_FOUND;
  }

  @Override
  protected Map<String, Object> fullCreateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("name", "Widget A");
    body.put("description", "A useful widget");
    body.put("quantity", 10);
    body.put("price", "19.99");
    body.put("occurredAt", "2026-05-01T00:00:00Z");
    body.put("status", "ACTIVE");
    body.put("tags", List.of("alpha", "beta"));
    return body;
  }

  @Override
  protected Map<String, Object> fullUpdateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("name", "Widget B");
    body.put("description", "Replaced description");
    body.put("quantity", 42);
    body.put("price", "99.50");
    body.put("occurredAt", "2026-06-01T00:00:00Z");
    body.put("status", "ARCHIVED");
    body.put("tags", List.of("gamma"));
    return body;
  }

  @Override
  protected String requiredFieldName() {
    return "name";
  }

  @Override
  protected int requiredFieldMaxLength() {
    return 200;
  }

  @Override
  protected String patchFieldName() {
    return "name";
  }

  @Override
  protected Object patchFieldValue() {
    return "renamed";
  }

  @Override
  protected Object patchFieldOriginalValue() {
    return "Widget A";
  }

  @Override
  protected String ukrainianNotFoundPrefix() {
    return "Приклад";
  }

  @Nested
  class ExampleCreateEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void adminCreatesWithAllFields_returnsFullPayload() {
      Map<String, Object> body = fullCreateBody();

      var created =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(body)
              .post(path())
              .then()
              .statusCode(CREATED.value())
              .extract()
              .response();

      assertThat(created.jsonPath().getString("description")).isEqualTo("A useful widget");
      assertThat(created.jsonPath().getInt("quantity")).isEqualTo(10);
      assertThat(created.jsonPath().getObject("price", BigDecimal.class))
          .isEqualByComparingTo(new BigDecimal("19.99"));
      assertThat(Instant.parse(created.jsonPath().getString("occurredAt")))
          .isEqualTo(Instant.parse("2026-05-01T00:00:00Z"));
      assertThat(created.jsonPath().getString("status")).isEqualTo("ACTIVE");
      assertThat(created.jsonPath().getList("tags", String.class))
          .containsExactlyInAnyOrder("alpha", "beta");
    }

    @Test
    void adminCreatesWithOnlyRequiredFields_optionalFieldsAreNull() {
      var body = new LinkedHashMap<String, Object>();
      body.put("name", "minimal");
      body.put("status", "DRAFT");

      var created =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(body)
              .post(path())
              .then()
              .statusCode(CREATED.value())
              .extract()
              .response();

      assertThat(created.jsonPath().getString("name")).isEqualTo("minimal");
      assertThat(created.jsonPath().getString("status")).isEqualTo("DRAFT");
      assertThat((Object) created.jsonPath().get("description")).isNull();
      assertThat((Object) created.jsonPath().get("quantity")).isNull();
      assertThat((Object) created.jsonPath().get("price")).isNull();
      assertThat((Object) created.jsonPath().get("occurredAt")).isNull();
      assertThat(created.jsonPath().getList("tags", String.class)).isEmpty();
    }

    @Test
    void adminCreatesWithTags_persistsTags() {
      Map<String, Object> body = fullCreateBody();
      body.put("tags", List.of("red", "green", "blue"));

      UUID id = createAsAdmin(body);

      var fetched =
          asUser().get(resourcePath(), id).then().statusCode(OK.value()).extract().response();
      assertThat(fetched.jsonPath().getList("tags", String.class))
          .containsExactlyInAnyOrder("red", "green", "blue");
    }

    @Test
    void invalidStatusEnumValue_returns400() {
      Map<String, Object> body = fullCreateBody();
      body.put("status", "NOT_A_STATUS");

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(body)
              .post(path())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(BAD_REQUEST.value());
    }
  }

  @Nested
  class ExampleGetByIdEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void userReadsEntityWithEmptyTags_tagsIsEmpty() {
      Map<String, Object> body = fullCreateBody();
      body.put("tags", List.of());
      UUID id = createAsAdmin(body);

      var response =
          asUser().get(resourcePath(), id).then().statusCode(OK.value()).extract().response();
      assertThat(response.jsonPath().getList("tags", String.class)).isEmpty();
    }
  }

  @Nested
  class ExampleSearchEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
      for (int i = 0; i < 19; i++) {
        createAsAdmin(fullCreateBody());
      }
      var different = new LinkedHashMap<>(fullCreateBody());
      different.put("name", "Gadget Z");
      createAsAdmin(different);
    }

    @Test
    void userFilteringByNameContains_returnsMatchingSubset() {
      var response =
          asUser()
              .queryParam("name", "Widget")
              .get(path())
              .then()
              .statusCode(OK.value())
              .extract()
              .response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(19L);
      assertThat(response.jsonPath().getList("content.name", String.class))
          .allMatch(name -> name.toLowerCase().contains("widget"));
    }

    @Test
    void userFilteringByStatusInDraftActive_returnsMatchingSubset() {
      var response =
          asUser()
              .queryParam("status", "DRAFT", "ACTIVE")
              .get(path())
              .then()
              .statusCode(OK.value())
              .extract()
              .response();
      var statuses = response.jsonPath().getList("content.status", String.class);
      assertThat(statuses).allMatch(s -> s.equals("DRAFT") || s.equals("ACTIVE"));
    }

    @Test
    void userFilteringByPriceRange_returnsMatchingSubset() {
      var response =
          asUser()
              .queryParam("minPrice", "0.00")
              .queryParam("maxPrice", "10000.00")
              .get(path())
              .then()
              .statusCode(OK.value())
              .extract()
              .response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(20L);
    }

    @Test
    void userFilteringByTags_returnsEntitiesWithAnyOfTheTags() {
      var response =
          asUser()
              .queryParam("tag", "alpha")
              .get(path())
              .then()
              .statusCode(OK.value())
              .extract()
              .response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(20L);
      List<List<String>> matchedTags = response.jsonPath().getList("content.tags");
      assertThat(matchedTags).allMatch(t -> t != null && t.contains("alpha"));
    }

    @Test
    void userWithInvalidStatusValueInQuery_returns400() {
      var response = asUser().queryParam("status", "NOPE").get(path()).then().extract().response();
      assertThat(response).hasStatus(BAD_REQUEST.value());
    }

    @Test
    void userMinPriceGreaterThanMaxPrice_returnsEmptyResultNotError() {
      var response =
          asUser()
              .queryParam("minPrice", "1000.00")
              .queryParam("maxPrice", "1.00")
              .get(path())
              .then()
              .statusCode(OK.value())
              .extract()
              .response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(0L);
      assertThat(response.jsonPath().getList("content")).isEmpty();
    }

    @Test
    void userMalformedDateInOccurredFrom_returns400() {
      var response =
          asUser().queryParam("occurredFrom", "not-a-date").get(path()).then().extract().response();
      assertThat(response).hasStatus(BAD_REQUEST.value());
    }

    @Test
    void userMalformedIntegerInMinQuantity_returns400() {
      var response =
          asUser().queryParam("minQuantity", "abc").get(path()).then().extract().response();
      assertThat(response).hasStatus(BAD_REQUEST.value());
    }
  }

  @Nested
  class ExampleReplaceEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void adminReplacesWithEmptyTags_returns200() {
      UUID id = createAsAdmin(fullCreateBody());
      Map<String, Object> body = fullUpdateBody();
      body.put("tags", List.of());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(body)
              .put(resourcePath(), id)
              .then()
              .statusCode(OK.value())
              .extract()
              .response();
      assertThat(response.jsonPath().getList("tags", String.class)).isEmpty();
    }

    @Test
    void adminReplacesAllFields_returnsUpdatedPayload() {
      UUID id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(fullUpdateBody())
              .put(resourcePath(), id)
              .then()
              .statusCode(OK.value())
              .extract()
              .response();
      assertThat(response.jsonPath().getString("status")).isEqualTo("ARCHIVED");
      assertThat(response.jsonPath().getInt("quantity")).isEqualTo(42);
    }
  }

  @Nested
  class ExamplePatchEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void adminPatchesMultipleFields_onlyThoseChanged() {
      UUID id = createAsAdmin(fullCreateBody());
      var patch = new LinkedHashMap<String, Object>();
      patch.put("name", "multi");
      patch.put("quantity", 99);

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(patch)
              .patch(resourcePath(), id)
              .then()
              .statusCode(OK.value())
              .extract()
              .response();
      assertThat(response.jsonPath().getString("name")).isEqualTo("multi");
      assertThat(response.jsonPath().getInt("quantity")).isEqualTo(99);
      assertThat(response.jsonPath().getString("description")).isEqualTo("A useful widget");
      assertThat(response.jsonPath().getString("status")).isEqualTo("ACTIVE");
    }

    @Test
    void adminPatchingTagsReplacesTheTagSet() {
      UUID id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(Map.of("tags", List.of("only")))
              .patch(resourcePath(), id)
              .then()
              .statusCode(OK.value())
              .extract()
              .response();
      assertThat(response.jsonPath().getList("tags", String.class)).containsExactly("only");
    }
  }
}
