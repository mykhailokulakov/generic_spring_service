package io.github.mykhailokulakov.genericspringservice.support.contract;

import static io.github.mykhailokulakov.genericspringservice.support.assertions.Assertions.assertThat;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asAdmin;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUnauthenticated;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUser;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
import io.github.mykhailokulakov.genericspringservice.support.IntegrationTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import io.restassured.http.ContentType;
import java.util.LinkedHashMap;
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
public abstract class AbstractCrudControllerTestContract<
    E extends SoftDeletable, M extends DomainModel> {

  @LocalServerPort protected int port;
  @Autowired protected DatabaseStateHelper db;

  protected abstract String path();

  protected abstract Class<E> entityClass();

  protected abstract String notFoundCode();

  protected abstract Map<String, Object> fullCreateBody();

  protected abstract Map<String, Object> fullUpdateBody();

  protected abstract String requiredFieldName();

  protected abstract int requiredFieldMaxLength();

  protected abstract String patchFieldName();

  protected abstract Object patchFieldValue();

  protected abstract Object patchFieldOriginalValue();

  protected abstract String ukrainianNotFoundPrefix();

  protected UUID createAsAdmin(Map<String, Object> body) {
    var response =
        asAdmin()
            .contentType(ContentType.JSON)
            .body(body)
            .post(path())
            .then()
            .statusCode(201)
            .extract()
            .response();
    return UUID.fromString(response.jsonPath().getString("id"));
  }

  protected void setUpDependencies() {}

  @Nested
  class CreateEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
      setUpDependencies();
    }

    @Test
    void adminCreatesWithAllFields_returns201_andGetReturnsSame() {
      Map<String, Object> body = fullCreateBody();

      var created =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(body)
              .post(path())
              .then()
              .statusCode(201)
              .extract()
              .response();

      assertThat(created.jsonPath().getString(requiredFieldName()))
          .isEqualTo(body.get(requiredFieldName()));

      var id = UUID.fromString(created.jsonPath().getString("id"));
      var fetched = asUser().get(path() + "/" + id).then().statusCode(200).extract().response();
      assertThat(fetched.jsonPath().getString("id")).isEqualTo(id.toString());
      assertThat(fetched.jsonPath().getString(requiredFieldName()))
          .isEqualTo(body.get(requiredFieldName()));
    }

    @Test
    void adminCreateProducesAuditFields() {
      var created =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(fullCreateBody())
              .post(path())
              .then()
              .statusCode(201)
              .extract()
              .response();

      assertThat(created.jsonPath().getString("createdAt")).isNotBlank();
      assertThat(created.jsonPath().getString("updatedAt")).isNotBlank();
      assertThat(created.jsonPath().getInt("version")).isEqualTo(0);
    }

    @Test
    void adminCreatesTwice_bothReturn201() {
      UUID first = createAsAdmin(fullCreateBody());
      UUID second = createAsAdmin(fullCreateBody());

      assertThat(first).isNotEqualTo(second);
      assertThat(db.countIncludingDeleted(entityClass())).isEqualTo(2L);
    }

    @Test
    void noToken_returns401() {
      var response =
          asUnauthenticated()
              .contentType(ContentType.JSON)
              .body(fullCreateBody())
              .post(path())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userRole_returns403() {
      var response =
          asUser()
              .contentType(ContentType.JSON)
              .body(fullCreateBody())
              .post(path())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(403).hasCode("error.forbidden");
    }

    @Test
    void missingRequiredField_returns400() {
      Map<String, Object> body = new LinkedHashMap<>(fullCreateBody());
      body.remove(requiredFieldName());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(body)
              .post(path())
              .then()
              .extract()
              .response();
      assertThat(response)
          .hasStatus(400)
          .hasCode("error.validation.failed")
          .hasViolation(requiredFieldName(), "NotBlank");
    }

    @Test
    void requiredFieldTooLong_returns400() {
      Map<String, Object> body = new LinkedHashMap<>(fullCreateBody());
      body.put(requiredFieldName(), "x".repeat(requiredFieldMaxLength() + 1));

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(body)
              .post(path())
              .then()
              .extract()
              .response();
      assertThat(response)
          .hasStatus(400)
          .hasCode("error.validation.failed")
          .hasViolation(requiredFieldName(), "Size");
    }
  }

  @Nested
  class GetByIdEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
      setUpDependencies();
    }

    @Test
    void userReadsCreatedEntity_returns200() {
      UUID id = createAsAdmin(fullCreateBody());

      var response = asUser().get(path() + "/" + id).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getString("id")).isEqualTo(id.toString());
      assertThat(response.jsonPath().getString(requiredFieldName()))
          .isEqualTo(fullCreateBody().get(requiredFieldName()));
    }

    @Test
    void userReadsImmediatelyAfterCreate_version0() {
      UUID id = createAsAdmin(fullCreateBody());

      var response = asUser().get(path() + "/" + id).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getLong("version")).isEqualTo(0L);
    }

    @Test
    void userReadsAfterReplace_returnsNewValues() {
      UUID id = createAsAdmin(fullCreateBody());

      asAdmin()
          .contentType(ContentType.JSON)
          .header("If-Match", "0")
          .body(fullUpdateBody())
          .put(path() + "/" + id)
          .then()
          .statusCode(200);

      var response = asUser().get(path() + "/" + id).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getString(requiredFieldName()))
          .isEqualTo(fullUpdateBody().get(requiredFieldName()));
      assertThat(response.jsonPath().getLong("version")).isEqualTo(1L);
    }

    @Test
    void noToken_returns401() {
      var response =
          asUnauthenticated().get(path() + "/" + UUID.randomUUID()).then().extract().response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void nonExistentId_returns404() {
      var response = asUser().get(path() + "/" + UUID.randomUUID()).then().extract().response();
      assertThat(response).hasStatus(404).hasCode(notFoundCode());
    }

    @Test
    void invalidUuid_returns400() {
      var response = asUser().get(path() + "/not-a-uuid").then().extract().response();
      assertThat(response).hasStatus(400);
    }

    @Test
    void softDeletedEntity_returns404() {
      UUID id = createAsAdmin(fullCreateBody());
      asAdmin().delete(path() + "/" + id).then().statusCode(204);

      var response = asUser().get(path() + "/" + id).then().extract().response();
      assertThat(response).hasStatus(404).hasCode(notFoundCode());
    }

    @Test
    void acceptLanguageUkOn404_returnsLocalizedDetail() {
      var response =
          asUser()
              .header("Accept-Language", "uk")
              .get(path() + "/" + UUID.randomUUID())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(404).hasCode(notFoundCode());
      assertThat(response.jsonPath().getString("detail")).startsWith(ukrainianNotFoundPrefix());
    }
  }

  @Nested
  class SearchEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
      setUpDependencies();
    }

    @Test
    void userEmptyFilter_returnsAllCreated() {
      createAsAdmin(fullCreateBody());
      createAsAdmin(fullCreateBody());

      var response = asUser().get(path()).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(2L);
      assertThat(response.jsonPath().getList("content")).hasSize(2);
    }

    @Test
    void userPaging_returnsCorrectPage() {
      for (int i = 0; i < 5; i++) {
        createAsAdmin(fullCreateBody());
      }

      var response =
          asUser()
              .queryParam("page", 1)
              .queryParam("size", 2)
              .get(path())
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getInt("page")).isEqualTo(1);
      assertThat(response.jsonPath().getInt("size")).isEqualTo(2);
      assertThat(response.jsonPath().getList("content")).hasSize(2);
      assertThat(response.jsonPath().getBoolean("first")).isFalse();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(5L);
    }

    @Test
    void userSortByCreatedAtDesc_returnsOrdered() {
      for (int i = 0; i < 3; i++) {
        createAsAdmin(fullCreateBody());
      }

      var response =
          asUser()
              .queryParam("sort", "createdAt,desc")
              .queryParam("size", "10")
              .get(path())
              .then()
              .statusCode(200)
              .extract()
              .response();
      var createdAts = response.jsonPath().getList("content.createdAt", String.class);
      assertThat(createdAts).isSortedAccordingTo((a, b) -> b.compareTo(a));
    }

    @Test
    void noToken_returns401() {
      var response = asUnauthenticated().get(path()).then().extract().response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void deletedExcludedFromSearch() {
      UUID id = createAsAdmin(fullCreateBody());
      asAdmin().delete(path() + "/" + id).then().statusCode(204);

      var response = asUser().get(path()).then().statusCode(200).extract().response();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(0L);
    }
  }

  @Nested
  class ReplaceEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
      setUpDependencies();
    }

    @Test
    void adminReplacesAllFields_returns200_version1() {
      UUID id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(fullUpdateBody())
              .put(path() + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getString(requiredFieldName()))
          .isEqualTo(fullUpdateBody().get(requiredFieldName()));
      assertThat(response.jsonPath().getLong("version")).isEqualTo(1L);
    }

    @Test
    void adminReplacesTwice_version2() {
      UUID id = createAsAdmin(fullCreateBody());

      asAdmin()
          .contentType(ContentType.JSON)
          .header("If-Match", "0")
          .body(fullUpdateBody())
          .put(path() + "/" + id)
          .then()
          .statusCode(200);

      Map<String, Object> secondBody = new LinkedHashMap<>(fullUpdateBody());
      secondBody.put(requiredFieldName(), "second-replace-value");

      var second =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "1")
              .body(secondBody)
              .put(path() + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(second.jsonPath().getLong("version")).isEqualTo(2L);
    }

    @Test
    void replacingSoftDeleted_returns404() {
      UUID id = createAsAdmin(fullCreateBody());
      asAdmin().delete(path() + "/" + id).then().statusCode(204);

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(fullUpdateBody())
              .put(path() + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(404).hasCode(notFoundCode());
    }

    @Test
    void noToken_returns401() {
      var response =
          asUnauthenticated()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(fullUpdateBody())
              .put(path() + "/" + UUID.randomUUID())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userRole_returns403() {
      UUID id = createAsAdmin(fullCreateBody());
      var response =
          asUser()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(fullUpdateBody())
              .put(path() + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(403).hasCode("error.forbidden");
    }

    @Test
    void missingIfMatchHeader_returns412() {
      UUID id = createAsAdmin(fullCreateBody());
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(fullUpdateBody())
              .put(path() + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(412).hasCode("error.if-match.required");
    }

    @Test
    void staleIfMatch_returns409() {
      UUID id = createAsAdmin(fullCreateBody());
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "999")
              .body(fullUpdateBody())
              .put(path() + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(409).hasCode("error.optimistic-lock");
    }

    @Test
    void validationFailure_returns400() {
      UUID id = createAsAdmin(fullCreateBody());
      Map<String, Object> body = new LinkedHashMap<>(fullUpdateBody());
      body.remove(requiredFieldName());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(body)
              .put(path() + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response)
          .hasStatus(400)
          .hasCode("error.validation.failed")
          .hasViolation(requiredFieldName(), "NotBlank");
    }
  }

  @Nested
  class PatchEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
      setUpDependencies();
    }

    @Test
    void adminPatchesSingleField_returns200_onlyThatFieldChanged() {
      UUID id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(path() + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getString(patchFieldName()))
          .isEqualTo(String.valueOf(patchFieldValue()));
    }

    @Test
    void adminPatchesWithNullFields_leavesUnchanged() {
      UUID id = createAsAdmin(fullCreateBody());
      var patch = new LinkedHashMap<String, Object>();
      patch.put(patchFieldName(), null);

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(patch)
              .patch(path() + "/" + id)
              .then()
              .statusCode(200)
              .extract()
              .response();
      assertThat(response.jsonPath().getString(patchFieldName()))
          .isEqualTo(String.valueOf(patchFieldOriginalValue()));
    }

    @Test
    void adminPatchIncrementsVersion() {
      UUID id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(path() + "/" + id)
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
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(path() + "/" + UUID.randomUUID())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userRole_returns403() {
      UUID id = createAsAdmin(fullCreateBody());
      var response =
          asUser()
              .contentType(ContentType.JSON)
              .header("If-Match", "0")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(path() + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(403).hasCode("error.forbidden");
    }

    @Test
    void missingIfMatch_returns412() {
      UUID id = createAsAdmin(fullCreateBody());
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(path() + "/" + id)
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(412).hasCode("error.if-match.required");
    }

    @Test
    void staleIfMatch_returns409() {
      UUID id = createAsAdmin(fullCreateBody());
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header("If-Match", "999")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(path() + "/" + id)
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
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(path() + "/" + UUID.randomUUID())
              .then()
              .extract()
              .response();
      assertThat(response).hasStatus(404).hasCode(notFoundCode());
    }
  }

  @Nested
  class DeleteEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
      setUpDependencies();
    }

    @Test
    void adminDeletes_returns204_thenGetReturns404() {
      UUID id = createAsAdmin(fullCreateBody());

      asAdmin().delete(path() + "/" + id).then().statusCode(204);

      var response = asUser().get(path() + "/" + id).then().extract().response();
      assertThat(response).hasStatus(404).hasCode(notFoundCode());
    }

    @Test
    void deletedRowRemainsInDb() {
      UUID id = createAsAdmin(fullCreateBody());
      asAdmin().delete(path() + "/" + id).then().statusCode(204);

      assertThat(db.countIncludingDeleted(entityClass())).isEqualTo(1L);
      assertThat(db.countWhereDeleted(entityClass())).isEqualTo(1L);
    }

    @Test
    void secondDeleteOnSameId_returns404() {
      UUID id = createAsAdmin(fullCreateBody());
      asAdmin().delete(path() + "/" + id).then().statusCode(204);

      var response = asAdmin().delete(path() + "/" + id).then().extract().response();
      assertThat(response).hasStatus(404).hasCode(notFoundCode());
    }

    @Test
    void deleteThenRecreateSameData_returns201() {
      UUID first = createAsAdmin(fullCreateBody());
      asAdmin().delete(path() + "/" + first).then().statusCode(204);

      UUID second = createAsAdmin(fullCreateBody());
      assertThat(second).isNotEqualTo(first);
    }

    @Test
    void noToken_returns401() {
      var response =
          asUnauthenticated().delete(path() + "/" + UUID.randomUUID()).then().extract().response();
      assertThat(response).hasStatus(401);
    }

    @Test
    void userRole_returns403() {
      UUID id = createAsAdmin(fullCreateBody());
      var response = asUser().delete(path() + "/" + id).then().extract().response();
      assertThat(response).hasStatus(403).hasCode("error.forbidden");
    }

    @Test
    void nonExistentId_returns404() {
      var response = asAdmin().delete(path() + "/" + UUID.randomUUID()).then().extract().response();
      assertThat(response).hasStatus(404).hasCode(notFoundCode());
    }

    @Test
    void invalidUuid_returns400() {
      var response = asAdmin().delete(path() + "/not-a-uuid").then().extract().response();
      assertThat(response).hasStatus(400);
    }
  }
}
