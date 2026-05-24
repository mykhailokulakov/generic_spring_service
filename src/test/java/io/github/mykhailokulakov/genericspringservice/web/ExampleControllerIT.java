package io.github.mykhailokulakov.genericspringservice.web;

import static io.github.mykhailokulakov.genericspringservice.support.assertions.Assertions.assertThat;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asAdmin;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUnauthenticated;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUser;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.support.IntegrationTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.WithSeededExamples;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@IntegrationTest
@Import(DatabaseStateHelper.class)
class ExampleControllerIT {

  private static final String PATH = "/api/v1/examples";

  @LocalServerPort int port;
  @Autowired DatabaseStateHelper db;

  // --- helpers ---------------------------------------------------------------

  private static Map<String, Object> fullCreateBody() {
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

  private static Map<String, Object> minimalCreateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("name", "minimal");
    body.put("status", "DRAFT");
    return body;
  }

  private static Map<String, Object> fullUpdateBody() {
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

  private UUID createExampleAsAdmin(Map<String, Object> body) {
    var response =
        asAdmin()
            .contentType(ContentType.JSON)
            .body(body)
            .post(PATH)
            .then()
            .statusCode(201)
            .extract()
            .response();
    return UUID.fromString(response.jsonPath().getString("id"));
  }

  // --- POST /api/v1/examples -------------------------------------------------

  @Nested
  class CreateEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void adminCreatesWithAllFields_returns201_andGetReturnsSame() {
      Map<String, Object> body = fullCreateBody();

      var created =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(body)
              .post(PATH)
              .then()
              .statusCode(201)
              .extract()
              .response();

      assertThat(created.jsonPath().getString("name")).isEqualTo("Widget A");
      assertThat(created.jsonPath().getString("description")).isEqualTo("A useful widget");
      assertThat(created.jsonPath().getInt("quantity")).isEqualTo(10);
      assertThat(created.jsonPath().getObject("price", BigDecimal.class))
          .isEqualByComparingTo(new BigDecimal("19.99"));
      assertThat(Instant.parse(created.jsonPath().getString("occurredAt")))
          .isEqualTo(Instant.parse("2026-05-01T00:00:00Z"));
      assertThat(created.jsonPath().getString("status")).isEqualTo("ACTIVE");
      assertThat(created.jsonPath().getList("tags", String.class))
          .containsExactlyInAnyOrder("alpha", "beta");

      var id = UUID.fromString(created.jsonPath().getString("id"));
      var fetched = asUser().get(PATH + "/" + id).then().statusCode(200).extract().response();
      assertThat(fetched.jsonPath().getString("id")).isEqualTo(id.toString());
      assertThat(fetched.jsonPath().getString("name")).isEqualTo("Widget A");
      assertThat(fetched.jsonPath().getString("description")).isEqualTo("A useful widget");
    }

    @Test
    void adminCreatesWithOnlyRequiredFields_returns201_andOptionalFieldsAreNull() {
      var created =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(minimalCreateBody())
              .post(PATH)
              .then()
              .statusCode(201)
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
    void adminCreatesWithTags_persistsTags_andGetReturnsThem() {
      Map<String, Object> body = fullCreateBody();
      body.put("tags", List.of("red", "green", "blue"));

      UUID id = createExampleAsAdmin(body);

      var fetched = asUser().get(PATH + "/" + id).then().statusCode(200).extract().response();
      assertThat(fetched.jsonPath().getList("tags", String.class))
          .containsExactlyInAnyOrder("red", "green", "blue");
    }

    @Test
    void adminCreatesTwiceWithSameData_bothReturn201_noUniqueConstraint() {
      Map<String, Object> body = fullCreateBody();
      UUID first = createExampleAsAdmin(body);
      UUID second = createExampleAsAdmin(body);

      assertThat(first).isNotEqualTo(second);
      assertThat(db.countIncludingDeleted(ExampleEntity.class)).isEqualTo(2L);
    }

    @Test
    void adminCreateProducesCreatedAtAndUpdatedAt() {
      var created =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(fullCreateBody())
              .post(PATH)
              .then()
              .statusCode(201)
              .extract()
              .response();

      assertThat(created.jsonPath().getString("createdAt")).isNotBlank();
      assertThat(created.jsonPath().getString("updatedAt")).isNotBlank();
      assertThat(created.jsonPath().getInt("version")).isEqualTo(0);
    }

    @Test
    void noToken_returns401() {
      var response =
          asUnauthenticated()
              .contentType(ContentType.JSON)
              .body(fullCreateBody())
              .post(PATH)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userRoleNotAdmin_returns403() {
      var response =
          asUser()
              .contentType(ContentType.JSON)
              .body(fullCreateBody())
              .post(PATH)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(403).hasCode("error.forbidden");
    }

    @Test
    void missingRequiredFieldName_returns400_withViolationOnNameField() {
      Map<String, Object> body = fullCreateBody();
      body.remove("name");

      var response =
          asAdmin().contentType(ContentType.JSON).body(body).post(PATH).then().extract().response();
      assertThat(response)
          .hasStatus(400)
          .hasCode("error.validation.failed")
          .hasViolation("name", "NotBlank");
    }

    @Test
    void nameTooLong_returns400_withViolationEntry() {
      Map<String, Object> body = fullCreateBody();
      body.put("name", "x".repeat(201));

      var response =
          asAdmin().contentType(ContentType.JSON).body(body).post(PATH).then().extract().response();
      assertThat(response)
          .hasStatus(400)
          .hasCode("error.validation.failed")
          .hasViolation("name", "Size");
    }

    @Test
    void invalidStatusEnumValue_returns400() {
      Map<String, Object> body = fullCreateBody();
      body.put("status", "NOT_A_STATUS");

      var response =
          asAdmin().contentType(ContentType.JSON).body(body).post(PATH).then().extract().response();
      assertThat(response).hasStatus(400);
    }
  }

  // --- GET /api/v1/examples/{id} ---------------------------------------------

  @Nested
  class GetByIdEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void userReadsAdminCreatedEntity_returns200_withFullBody() {
      UUID id = createExampleAsAdmin(fullCreateBody());

      var response = asUser().get(PATH + "/" + id).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getString("id")).isEqualTo(id.toString());
      assertThat(response.jsonPath().getString("name")).isEqualTo("Widget A");
      assertThat(response.jsonPath().getString("status")).isEqualTo("ACTIVE");
    }

    @Test
    void userReadsEntityWithAllFieldTypesPopulated_returns200_allFieldsPresent() {
      UUID id = createExampleAsAdmin(fullCreateBody());

      var response = asUser().get(PATH + "/" + id).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getString("id")).isNotBlank();
      assertThat(response.jsonPath().getString("name")).isNotBlank();
      assertThat(response.jsonPath().getString("description")).isNotBlank();
      assertThat(response.jsonPath().getInt("quantity")).isEqualTo(10);
      assertThat(response.jsonPath().getString("price")).isNotBlank();
      assertThat(response.jsonPath().getString("occurredAt")).isNotBlank();
      assertThat(response.jsonPath().getString("status")).isEqualTo("ACTIVE");
      assertThat(response.jsonPath().getList("tags", String.class)).isNotEmpty();
      assertThat(response.jsonPath().getString("createdAt")).isNotBlank();
      assertThat(response.jsonPath().getString("updatedAt")).isNotBlank();
      assertThat(response.jsonPath().getLong("version")).isEqualTo(0L);
    }

    @Test
    void userReadsEntityWithEmptyTags_returns200_tagsIsEmpty() {
      Map<String, Object> body = fullCreateBody();
      body.put("tags", List.of());
      UUID id = createExampleAsAdmin(body);

      var response = asUser().get(PATH + "/" + id).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getList("tags", String.class)).isEmpty();
    }

    @Test
    void userReadsImmediatelyAfterCreate_returns200_version0() {
      UUID id = createExampleAsAdmin(fullCreateBody());

      var response = asUser().get(PATH + "/" + id).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getLong("version")).isEqualTo(0L);
    }

    @Test
    void userReadsAfterAdminUpdate_returns200_newValues() {
      UUID id = createExampleAsAdmin(fullCreateBody());

      asAdmin()
          .contentType(ContentType.JSON)
          .header("If-Match", "0")
          .body(fullUpdateBody())
          .put(PATH + "/" + id)
          .then()
          .statusCode(200);

      var response = asUser().get(PATH + "/" + id).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getString("name")).isEqualTo("Widget B");
      assertThat(response.jsonPath().getString("status")).isEqualTo("ARCHIVED");
      assertThat(response.jsonPath().getLong("version")).isEqualTo(1L);
    }

    @Test
    void noToken_returns401() {
      var response =
          asUnauthenticated().get(PATH + "/" + UUID.randomUUID()).then().extract().response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userReadsNonExistentId_returns404_withExampleNotFoundCode() {
      var response = asUser().get(PATH + "/" + UUID.randomUUID()).then().extract().response();
      assertThat(response).hasStatus(404).hasCode("error.example.not-found");
    }

    @Test
    void userReadsInvalidUuidFormat_returns400() {
      var response = asUser().get(PATH + "/not-a-uuid").then().extract().response();
      assertThat(response).hasStatus(400);
    }

    @Test
    void userReadsSoftDeletedEntity_returns404() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      asAdmin().delete(PATH + "/" + id).then().statusCode(204);

      var response = asUser().get(PATH + "/" + id).then().extract().response();
      assertThat(response).hasStatus(404).hasCode("error.example.not-found");
    }

    @Test
    void acceptLanguageEsOn404_returnsSpanishDetail() {
      var response =
          asUser()
              .header("Accept-Language", "es")
              .get(PATH + "/" + UUID.randomUUID())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(404).hasCode("error.example.not-found");
      assertThat(response.jsonPath().getString("detail")).startsWith("No se encontró");
    }
  }

  // --- GET /api/v1/examples (search) -----------------------------------------

  @Nested
  @WithSeededExamples(
      count = 20,
      tags = {"searchable"})
  class SearchEndpoint {

    @Test
    void userEmptyFilter_returnsPagedResults_totalIs20() {
      var response = asUser().get(PATH).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(20L);
      assertThat(response.jsonPath().getList("content")).isNotEmpty();
    }

    @Test
    void userFilteringByNameContains_returnsMatchingSubset() {
      String anyName =
          asUser()
              .get(PATH)
              .then()
              .statusCode(200)
              .extract()
              .response()
              .jsonPath()
              .getString("content[0].name");

      var response =
          asUser()
              .queryParam("name", anyName)
              .get(PATH)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getLong("totalElements")).isGreaterThanOrEqualTo(1L);
      assertThat(response.jsonPath().getList("content.name", String.class))
          .allMatch(name -> name.toLowerCase().contains(anyName.toLowerCase()));
    }

    @Test
    void userFilteringByStatusInDraftActive_returnsMatchingSubset() {
      var response =
          asUser()
              .queryParam("statuses", "DRAFT", "ACTIVE")
              .get(PATH)
              .then()
              .statusCode(200)
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
              .get(PATH)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(20L);
    }

    @Test
    void userFilteringByTags_returnsEntitiesWithAnyOfTheTags() {
      // @WithSeededExamples(tags = {"searchable"}) guarantees every seeded
      // entity carries this tag, so the filter has a deterministic target.
      var response =
          asUser()
              .queryParam("tags", "searchable")
              .get(PATH)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(20L);
      List<List<String>> matchedTags = response.jsonPath().getList("content.tags");
      assertThat(matchedTags).allMatch(t -> t != null && t.contains("searchable"));
    }

    @Test
    void userSortByCreatedAtDesc_returnsOrderedCorrectly() {
      var response =
          asUser()
              .queryParam("sort", "createdAt,desc")
              .queryParam("size", "20")
              .get(PATH)
              .then()
              .statusCode(200)
              .extract()
              .response();
      var createdAts = response.jsonPath().getList("content.createdAt", String.class);
      assertThat(createdAts).isSortedAccordingTo((a, b) -> b.compareTo(a));
    }

    @Test
    void userPaging_page1Size5_returnsSecondPage() {
      var response =
          asUser()
              .queryParam("page", 1)
              .queryParam("size", 5)
              .get(PATH)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getInt("page")).isEqualTo(1);
      assertThat(response.jsonPath().getInt("size")).isEqualTo(5);
      assertThat(response.jsonPath().getList("content")).hasSize(5);
      assertThat(response.jsonPath().getBoolean("first")).isFalse();
    }

    @Test
    void noToken_returns401() {
      var response = asUnauthenticated().get(PATH).then().extract().response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userWithInvalidStatusValueInQuery_returns400() {
      var response = asUser().queryParam("statuses", "NOPE").get(PATH).then().extract().response();
      assertThat(response).hasStatus(400);
    }

    @Test
    void userMinPriceGreaterThanMaxPrice_returnsEmptyResultNotError() {
      var response =
          asUser()
              .queryParam("minPrice", "1000.00")
              .queryParam("maxPrice", "1.00")
              .get(PATH)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(0L);
      assertThat(response.jsonPath().getList("content")).isEmpty();
    }

    @Test
    void userMalformedDateInOccurredFrom_returns400() {
      var response =
          asUser().queryParam("occurredFrom", "not-a-date").get(PATH).then().extract().response();
      assertThat(response).hasStatus(400);
    }

    @Test
    void userMalformedIntegerInMinQuantity_returns400() {
      var response =
          asUser().queryParam("minQuantity", "abc").get(PATH).then().extract().response();
      assertThat(response).hasStatus(400);
    }
  }

  // --- PUT /api/v1/examples/{id} ---------------------------------------------

  @Nested
  class ReplaceEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void adminReplacesAllFields_returns200_newState_version1() {
      UUID id = createExampleAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(fullUpdateBody())
              .put(PATH + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getString("name")).isEqualTo("Widget B");
      assertThat(response.jsonPath().getString("status")).isEqualTo("ARCHIVED");
      assertThat(response.jsonPath().getInt("quantity")).isEqualTo(42);
      assertThat(response.jsonPath().getLong("version")).isEqualTo(1L);
    }

    @Test
    void adminReplacesWithEmptyTags_returns200() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      Map<String, Object> body = fullUpdateBody();
      body.put("tags", List.of());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(body)
              .put(PATH + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getList("tags", String.class)).isEmpty();
    }

    @Test
    void adminReplacesTwice_bothSucceed_version2() {
      UUID id = createExampleAsAdmin(fullCreateBody());

      asAdmin()
          .contentType(ContentType.JSON)
          .header("If-Match", "0")
          .body(fullUpdateBody())
          .put(PATH + "/" + id)
          .then()
          .statusCode(200);

      var second =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "1")
              .body(fullUpdateBody())
              .put(PATH + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(second.jsonPath().getLong("version")).isEqualTo(2L);
    }

    @Test
    void adminReplacingSoftDeleted_returns404() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      asAdmin().delete(PATH + "/" + id).then().statusCode(204);

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(fullUpdateBody())
              .put(PATH + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(404).hasCode("error.example.not-found");
    }

    @Test
    void acceptLanguageUnaffectedBySuccessPath() {
      UUID id = createExampleAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .header("Accept-Language", "es")
              .body(fullUpdateBody())
              .put(PATH + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getString("name")).isEqualTo("Widget B");
      assertThat(response.jsonPath().getLong("version")).isEqualTo(1L);
    }

    @Test
    void noToken_returns401() {
      var response =
          asUnauthenticated()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(fullUpdateBody())
              .put(PATH + "/" + UUID.randomUUID())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userRole_returns403() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var response =
          asUser()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(fullUpdateBody())
              .put(PATH + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(403).hasCode("error.forbidden");
    }

    @Test
    void missingIfMatchHeader_returnsIfMatchRequired() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(fullUpdateBody())
              .put(PATH + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(412).hasCode("error.if-match.required");
    }

    @Test
    void staleIfMatch_returns409_optimisticLockCode() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "999")
              .body(fullUpdateBody())
              .put(PATH + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(409).hasCode("error.optimistic-lock");
    }

    @Test
    void validationFailureOnBody_returns400() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      Map<String, Object> body = fullUpdateBody();
      body.remove("name");

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(body)
              .put(PATH + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response)
          .hasStatus(400)
          .hasCode("error.validation.failed")
          .hasViolation("name", "NotBlank");
    }
  }

  // --- PATCH /api/v1/examples/{id} -------------------------------------------

  @Nested
  class PatchEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void adminPatchesSingleField_returns200_onlyThatFieldChanged() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var patch = Map.<String, Object>of("name", "renamed");

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(patch)
              .patch(PATH + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getString("name")).isEqualTo("renamed");
      assertThat(response.jsonPath().getString("description")).isEqualTo("A useful widget");
      assertThat(response.jsonPath().getInt("quantity")).isEqualTo(10);
      assertThat(response.jsonPath().getString("status")).isEqualTo("ACTIVE");
    }

    @Test
    void adminPatchesMultipleFields_returns200_onlyThoseChanged() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var patch = new LinkedHashMap<String, Object>();
      patch.put("name", "multi");
      patch.put("quantity", 99);

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(patch)
              .patch(PATH + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getString("name")).isEqualTo("multi");
      assertThat(response.jsonPath().getInt("quantity")).isEqualTo(99);
      assertThat(response.jsonPath().getString("description")).isEqualTo("A useful widget");
      assertThat(response.jsonPath().getString("status")).isEqualTo("ACTIVE");
    }

    @Test
    void adminPatchesWithNullFields_leavesFieldsUnchanged() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var patch = new LinkedHashMap<String, Object>();
      patch.put("name", null);
      patch.put("description", null);
      patch.put("quantity", null);

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(patch)
              .patch(PATH + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getString("name")).isEqualTo("Widget A");
      assertThat(response.jsonPath().getString("description")).isEqualTo("A useful widget");
      assertThat(response.jsonPath().getInt("quantity")).isEqualTo(10);
    }

    @Test
    void adminPatchingTagsReplacesTheTagSet() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var patch = Map.<String, Object>of("tags", List.of("only"));

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(patch)
              .patch(PATH + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getList("tags", String.class)).containsExactly("only");
    }

    @Test
    void adminPatchIncrementsVersion() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var patch = Map.<String, Object>of("name", "bumped");

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(patch)
              .patch(PATH + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getLong("version")).isEqualTo(1L);
    }

    @Test
    void noToken_returns401() {
      var response =
          asUnauthenticated()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(Map.of("name", "x"))
              .patch(PATH + "/" + UUID.randomUUID())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userRole_returns403() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var response =
          asUser()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(Map.of("name", "x"))
              .patch(PATH + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(403).hasCode("error.forbidden");
    }

    @Test
    void missingIfMatch_returnsIfMatchRequired() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(Map.of("name", "x"))
              .patch(PATH + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(412).hasCode("error.if-match.required");
    }

    @Test
    void staleIfMatch_returns409() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "999")
              .body(Map.of("name", "x"))
              .patch(PATH + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(409).hasCode("error.optimistic-lock");
    }

    @Test
    void nonExistentId_returns404() {
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(Map.of("name", "x"))
              .patch(PATH + "/" + UUID.randomUUID())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(404).hasCode("error.example.not-found");
    }
  }

  // --- DELETE /api/v1/examples/{id} ------------------------------------------

  @Nested
  class DeleteEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void adminDeletesExisting_returns204_andSubsequentGetReturns404() {
      UUID id = createExampleAsAdmin(fullCreateBody());

      asAdmin().delete(PATH + "/" + id).then().statusCode(204);

      var response = asUser().get(PATH + "/" + id).then().extract().response();
      assertThat(response).hasStatus(404).hasCode("error.example.not-found");
    }

    @Test
    void adminDeletesThenCreatesNewWithSameName_returns201() {
      UUID first = createExampleAsAdmin(fullCreateBody());
      asAdmin().delete(PATH + "/" + first).then().statusCode(204);

      UUID second = createExampleAsAdmin(fullCreateBody());
      assertThat(second).isNotEqualTo(first);
    }

    @Test
    void deletedRowRemainsInDb() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      asAdmin().delete(PATH + "/" + id).then().statusCode(204);

      assertThat(db.countIncludingDeleted(ExampleEntity.class)).isEqualTo(1L);
      assertThat(db.countWhereDeleted(ExampleEntity.class)).isEqualTo(1L);
    }

    @Test
    void deletedRowExcludedFromSearch() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      asAdmin().delete(PATH + "/" + id).then().statusCode(204);

      var response = asUser().get(PATH).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(0L);
    }

    @Test
    void secondDeleteOnSameId_returns404_notServerError() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      asAdmin().delete(PATH + "/" + id).then().statusCode(204);

      var response = asAdmin().delete(PATH + "/" + id).then().extract().response();
      assertThat(response).hasStatus(404).hasCode("error.example.not-found");
    }

    @Test
    void noToken_returns401() {
      var response =
          asUnauthenticated().delete(PATH + "/" + UUID.randomUUID()).then().extract().response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userRole_returns403() {
      UUID id = createExampleAsAdmin(fullCreateBody());
      var response = asUser().delete(PATH + "/" + id).then().extract().response();
      assertThat(response).hasStatus(403).hasCode("error.forbidden");
    }

    @Test
    void nonExistentId_returns404() {
      var response = asAdmin().delete(PATH + "/" + UUID.randomUUID()).then().extract().response();
      assertThat(response).hasStatus(404).hasCode("error.example.not-found");
    }

    @Test
    void invalidUuid_returns400() {
      var response = asAdmin().delete(PATH + "/not-a-uuid").then().extract().response();
      assertThat(response).hasStatus(400);
    }

    @Test
    void acceptLanguageEsOn404_returnsSpanishDetail() {
      var response =
          asAdmin()
              .header("Accept-Language", "es")
              .delete(PATH + "/" + UUID.randomUUID())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(404).hasCode("error.example.not-found");
      assertThat(response.jsonPath().getString("detail")).startsWith("No se encontró");
    }
  }
}
