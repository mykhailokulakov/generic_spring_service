package io.github.mykhailokulakov.genericspringservice.support.contract;

import static io.github.mykhailokulakov.genericspringservice.support.assertions.Assertions.assertThat;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asAdmin;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUnauthenticated;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asUser;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
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
import org.springframework.http.HttpHeaders;

@IntegrationTest
@Import(DatabaseStateHelper.class)
public abstract class AbstractCrudControllerTestContract<E extends SoftDeletable> {

  @LocalServerPort private int port;
  @Autowired protected DatabaseStateHelper db;

  protected abstract String path();

  protected abstract Class<E> entityClass();

  protected abstract ErrorCode notFoundCode();

  protected abstract Map<String, Object> fullCreateBody();

  protected abstract Map<String, Object> fullUpdateBody();

  protected abstract String requiredFieldName();

  protected abstract int requiredFieldMaxLength();

  protected abstract String patchFieldName();

  protected abstract Object patchFieldValue();

  protected abstract Object patchFieldOriginalValue();

  protected abstract String ukrainianNotFoundPrefix();

  protected String resourcePath() {
    return path() + "/{id}";
  }

  protected UUID createAsAdmin(Map<String, Object> body) {
    var response =
        asAdmin()
            .contentType(ContentType.JSON)
            .body(body)
            .post(path())
            .then()
            .statusCode(CREATED.value())
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
    void givenAdminAndFullBody_whenCreated_thenReturns201AndGetByIdReturnsSameEntity() {
      var body = fullCreateBody();

      var created =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(body)
              .post(path())
              .then()
              .statusCode(CREATED.value())
              .extract()
              .response();

      assertThat(created.jsonPath().getString(requiredFieldName()))
          .isEqualTo(body.get(requiredFieldName()));

      var id = UUID.fromString(created.jsonPath().getString("id"));
      var fetched =
          asUser().get(resourcePath(), id).then().statusCode(OK.value()).extract().response();
      assertThat(fetched.jsonPath().getString("id")).isEqualTo(id.toString());
      assertThat(fetched.jsonPath().getString(requiredFieldName()))
          .isEqualTo(body.get(requiredFieldName()));
    }

    @Test
    void givenAdmin_whenCreated_thenResponseContainsAuditFields() {
      var created =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(fullCreateBody())
              .post(path())
              .then()
              .statusCode(CREATED.value())
              .extract()
              .response();

      assertThat(created.jsonPath().getString("createdAt")).isNotBlank();
      assertThat(created.jsonPath().getString("updatedAt")).isNotBlank();
      assertThat(created.jsonPath().getInt("version")).isEqualTo(0);
    }

    @Test
    void givenAdmin_whenCreatedTwice_thenBothReturn201WithDistinctIds() {
      var first = createAsAdmin(fullCreateBody());
      var second = createAsAdmin(fullCreateBody());

      assertThat(first).isNotEqualTo(second);
      assertThat(db.countIncludingDeleted(entityClass())).isEqualTo(2L);
    }

    @Test
    void givenNoToken_whenCreated_thenReturns401() {
      var response =
          asUnauthenticated()
              .contentType(ContentType.JSON)
              .body(fullCreateBody())
              .post(path())
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(UNAUTHORIZED.value());
    }

    @Test
    void givenUserRole_whenCreated_thenReturns403() {
      var response =
          asUser()
              .contentType(ContentType.JSON)
              .body(fullCreateBody())
              .post(path())
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(FORBIDDEN.value()).hasCode(ErrorCode.FORBIDDEN.key());
    }

    @Test
    void givenBodyMissingRequiredField_whenCreated_thenReturns400WithNotBlankViolation() {
      var body = new LinkedHashMap<>(fullCreateBody());
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
          .hasStatus(BAD_REQUEST.value())
          .hasCode(ErrorCode.VALIDATION_FAILED.key())
          .hasViolation(
              requiredFieldName(), jakarta.validation.constraints.NotBlank.class.getSimpleName());
    }

    @Test
    void givenRequiredFieldExceedsMaxLength_whenCreated_thenReturns400WithSizeViolation() {
      var body = new LinkedHashMap<>(fullCreateBody());
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
          .hasStatus(BAD_REQUEST.value())
          .hasCode(ErrorCode.VALIDATION_FAILED.key())
          .hasViolation(
              requiredFieldName(), jakarta.validation.constraints.Size.class.getSimpleName());
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
    void givenExistingEntity_whenFetchedAsUser_thenReturns200WithEntity() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asUser().get(resourcePath(), id).then().statusCode(OK.value()).extract().response();

      assertThat(response.jsonPath().getString("id")).isEqualTo(id.toString());
      assertThat(response.jsonPath().getString(requiredFieldName()))
          .isEqualTo(fullCreateBody().get(requiredFieldName()));
    }

    @Test
    void givenFreshlyCreatedEntity_whenFetched_thenVersionIsZero() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asUser().get(resourcePath(), id).then().statusCode(OK.value()).extract().response();

      assertThat(response.jsonPath().getLong("version")).isEqualTo(0L);
    }

    @Test
    void givenEntityWasReplaced_whenFetched_thenReturnsNewValues() {
      var id = createAsAdmin(fullCreateBody());
      asAdmin()
          .contentType(ContentType.JSON)
          .header(HttpHeaders.IF_MATCH, "0")
          .body(fullUpdateBody())
          .put(resourcePath(), id)
          .then()
          .statusCode(OK.value());

      var response =
          asUser().get(resourcePath(), id).then().statusCode(OK.value()).extract().response();

      assertThat(response.jsonPath().getString(requiredFieldName()))
          .isEqualTo(fullUpdateBody().get(requiredFieldName()));
      assertThat(response.jsonPath().getLong("version")).isEqualTo(1L);
    }

    @Test
    void givenNoToken_whenFetched_thenReturns401() {
      var response =
          asUnauthenticated().get(resourcePath(), UUID.randomUUID()).then().extract().response();

      assertThat(response).hasStatus(UNAUTHORIZED.value());
    }

    @Test
    void givenUnknownId_whenFetched_thenReturns404() {
      var response = asUser().get(resourcePath(), UUID.randomUUID()).then().extract().response();

      assertThat(response).hasStatus(NOT_FOUND.value()).hasCode(notFoundCode().key());
    }

    @Test
    void givenInvalidUuid_whenFetched_thenReturns400() {
      var response = asUser().get(path() + "/not-a-uuid").then().extract().response();

      assertThat(response).hasStatus(BAD_REQUEST.value());
    }

    @Test
    void givenSoftDeletedEntity_whenFetched_thenReturns404() {
      var id = createAsAdmin(fullCreateBody());
      asAdmin().delete(resourcePath(), id).then().statusCode(NO_CONTENT.value());

      var response = asUser().get(resourcePath(), id).then().extract().response();

      assertThat(response).hasStatus(NOT_FOUND.value()).hasCode(notFoundCode().key());
    }

    @Test
    void givenAcceptLanguageUk_whenNotFound_thenReturnsLocalizedDetail() {
      var response =
          asUser()
              .header(HttpHeaders.ACCEPT_LANGUAGE, "uk")
              .get(resourcePath(), UUID.randomUUID())
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(NOT_FOUND.value()).hasCode(notFoundCode().key());
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
    void givenTwoEntities_whenSearchedWithoutFilter_thenReturnsBoth() {
      createAsAdmin(fullCreateBody());
      createAsAdmin(fullCreateBody());

      var response = asUser().get(path()).then().statusCode(OK.value()).extract().response();

      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(2L);
      assertThat(response.jsonPath().getList("content")).hasSize(2);
    }

    @Test
    void givenFiveEntities_whenSearchedWithPaging_thenReturnsRequestedPage() {
      for (int i = 0; i < 5; i++) {
        createAsAdmin(fullCreateBody());
      }

      var response =
          asUser()
              .queryParam("page", 1)
              .queryParam("size", 2)
              .get(path())
              .then()
              .statusCode(OK.value())
              .extract()
              .response();

      assertThat(response.jsonPath().getInt("page")).isEqualTo(1);
      assertThat(response.jsonPath().getInt("size")).isEqualTo(2);
      assertThat(response.jsonPath().getList("content")).hasSize(2);
      assertThat(response.jsonPath().getBoolean("first")).isFalse();
      assertThat(response.jsonPath().getLong("totalElements")).isEqualTo(5L);
    }

    @Test
    void givenThreeEntities_whenSortedByCreatedAtDesc_thenReturnedInDescendingOrder() {
      for (int i = 0; i < 3; i++) {
        createAsAdmin(fullCreateBody());
      }

      var response =
          asUser()
              .queryParam("sort", "createdAt,desc")
              .queryParam("size", "10")
              .get(path())
              .then()
              .statusCode(OK.value())
              .extract()
              .response();

      var createdAts = response.jsonPath().getList("content.createdAt", String.class);
      assertThat(createdAts).isSortedAccordingTo((a, b) -> b.compareTo(a));
    }

    @Test
    void givenNoToken_whenSearched_thenReturns401() {
      var response = asUnauthenticated().get(path()).then().extract().response();

      assertThat(response).hasStatus(UNAUTHORIZED.value());
    }

    @Test
    void givenSoftDeletedEntity_whenSearched_thenIsExcluded() {
      var id = createAsAdmin(fullCreateBody());
      asAdmin().delete(resourcePath(), id).then().statusCode(NO_CONTENT.value());

      var response = asUser().get(path()).then().statusCode(OK.value()).extract().response();

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
    void givenAdminAndMatchingIfMatch_whenReplaced_thenReturns200WithIncrementedVersion() {
      var id = createAsAdmin(fullCreateBody());

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

      assertThat(response.jsonPath().getString(requiredFieldName()))
          .isEqualTo(fullUpdateBody().get(requiredFieldName()));
      assertThat(response.jsonPath().getLong("version")).isEqualTo(1L);
    }

    @Test
    void givenAdmin_whenReplacedTwice_thenVersionAdvancesToTwo() {
      var id = createAsAdmin(fullCreateBody());
      asAdmin()
          .contentType(ContentType.JSON)
          .header(HttpHeaders.IF_MATCH, "0")
          .body(fullUpdateBody())
          .put(resourcePath(), id)
          .then()
          .statusCode(OK.value());
      var secondBody = new LinkedHashMap<>(fullUpdateBody());
      secondBody.put(requiredFieldName(), patchFieldValue());

      var second =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "1")
              .body(secondBody)
              .put(resourcePath(), id)
              .then()
              .statusCode(OK.value())
              .extract()
              .response();

      assertThat(second.jsonPath().getLong("version")).isEqualTo(2L);
    }

    @Test
    void givenSoftDeletedEntity_whenReplaced_thenReturns404() {
      var id = createAsAdmin(fullCreateBody());
      asAdmin().delete(resourcePath(), id).then().statusCode(NO_CONTENT.value());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(fullUpdateBody())
              .put(resourcePath(), id)
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(NOT_FOUND.value()).hasCode(notFoundCode().key());
    }

    @Test
    void givenNoToken_whenReplaced_thenReturns401() {
      var response =
          asUnauthenticated()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(fullUpdateBody())
              .put(resourcePath(), UUID.randomUUID())
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(UNAUTHORIZED.value());
    }

    @Test
    void givenUserRole_whenReplaced_thenReturns403() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asUser()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(fullUpdateBody())
              .put(resourcePath(), id)
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(FORBIDDEN.value()).hasCode(ErrorCode.FORBIDDEN.key());
    }

    @Test
    void givenMissingIfMatchHeader_whenReplaced_thenReturns412() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(fullUpdateBody())
              .put(resourcePath(), id)
              .then()
              .extract()
              .response();

      assertThat(response)
          .hasStatus(PRECONDITION_FAILED.value())
          .hasCode(ErrorCode.IF_MATCH_REQUIRED.key());
    }

    @Test
    void givenStaleIfMatch_whenReplaced_thenReturns409() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "999")
              .body(fullUpdateBody())
              .put(resourcePath(), id)
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(CONFLICT.value()).hasCode(ErrorCode.OPTIMISTIC_LOCK.key());
    }

    @Test
    void givenBodyMissingRequiredField_whenReplaced_thenReturns400WithNotBlankViolation() {
      var id = createAsAdmin(fullCreateBody());
      var body = new LinkedHashMap<>(fullUpdateBody());
      body.remove(requiredFieldName());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(body)
              .put(resourcePath(), id)
              .then()
              .extract()
              .response();

      assertThat(response)
          .hasStatus(BAD_REQUEST.value())
          .hasCode(ErrorCode.VALIDATION_FAILED.key())
          .hasViolation(
              requiredFieldName(), jakarta.validation.constraints.NotBlank.class.getSimpleName());
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
    void givenAdminAndSingleFieldPatch_whenPatched_thenOnlyThatFieldChanges() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(resourcePath(), id)
              .then()
              .statusCode(OK.value())
              .extract()
              .response();

      assertThat(response.jsonPath().getString(patchFieldName()))
          .isEqualTo(String.valueOf(patchFieldValue()));
    }

    @Test
    void givenPatchWithNullField_whenPatched_thenFieldIsUnchanged() {
      var id = createAsAdmin(fullCreateBody());
      var patch = new LinkedHashMap<String, Object>();
      patch.put(patchFieldName(), null);

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

      assertThat(response.jsonPath().getString(patchFieldName()))
          .isEqualTo(String.valueOf(patchFieldOriginalValue()));
    }

    @Test
    void givenAdmin_whenPatched_thenVersionIsIncremented() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(resourcePath(), id)
              .then()
              .statusCode(OK.value())
              .extract()
              .response();

      assertThat(response.jsonPath().getLong("version")).isEqualTo(1L);
    }

    @Test
    void givenNoToken_whenPatched_thenReturns401() {
      var response =
          asUnauthenticated()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(resourcePath(), UUID.randomUUID())
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(UNAUTHORIZED.value());
    }

    @Test
    void givenUserRole_whenPatched_thenReturns403() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asUser()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(resourcePath(), id)
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(FORBIDDEN.value()).hasCode(ErrorCode.FORBIDDEN.key());
    }

    @Test
    void givenMissingIfMatchHeader_whenPatched_thenReturns412() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(resourcePath(), id)
              .then()
              .extract()
              .response();

      assertThat(response)
          .hasStatus(PRECONDITION_FAILED.value())
          .hasCode(ErrorCode.IF_MATCH_REQUIRED.key());
    }

    @Test
    void givenStaleIfMatch_whenPatched_thenReturns409() {
      var id = createAsAdmin(fullCreateBody());

      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "999")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(resourcePath(), id)
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(CONFLICT.value()).hasCode(ErrorCode.OPTIMISTIC_LOCK.key());
    }

    @Test
    void givenUnknownId_whenPatched_thenReturns404() {
      var response =
          asAdmin()
              .contentType(ContentType.JSON)
              .header(HttpHeaders.IF_MATCH, "0")
              .body(Map.of(patchFieldName(), patchFieldValue()))
              .patch(resourcePath(), UUID.randomUUID())
              .then()
              .extract()
              .response();

      assertThat(response).hasStatus(NOT_FOUND.value()).hasCode(notFoundCode().key());
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
    void givenAdmin_whenDeleted_thenReturns204AndSubsequentGetReturns404() {
      var id = createAsAdmin(fullCreateBody());

      asAdmin().delete(resourcePath(), id).then().statusCode(NO_CONTENT.value());

      var response = asUser().get(resourcePath(), id).then().extract().response();
      assertThat(response).hasStatus(NOT_FOUND.value()).hasCode(notFoundCode().key());
    }

    @Test
    void givenAdmin_whenDeleted_thenRowRemainsInDatabaseAsSoftDeleted() {
      var id = createAsAdmin(fullCreateBody());

      asAdmin().delete(resourcePath(), id).then().statusCode(NO_CONTENT.value());

      assertThat(db.countIncludingDeleted(entityClass())).isEqualTo(1L);
      assertThat(db.countWhereDeleted(entityClass())).isEqualTo(1L);
    }

    @Test
    void givenAlreadyDeletedEntity_whenDeletedAgain_thenReturns404() {
      var id = createAsAdmin(fullCreateBody());
      asAdmin().delete(resourcePath(), id).then().statusCode(NO_CONTENT.value());

      var response = asAdmin().delete(resourcePath(), id).then().extract().response();

      assertThat(response).hasStatus(NOT_FOUND.value()).hasCode(notFoundCode().key());
    }

    @Test
    void givenDeletedEntity_whenRecreatedWithSameData_thenReturns201WithNewId() {
      var first = createAsAdmin(fullCreateBody());
      asAdmin().delete(resourcePath(), first).then().statusCode(NO_CONTENT.value());

      var second = createAsAdmin(fullCreateBody());

      assertThat(second).isNotEqualTo(first);
    }

    @Test
    void givenNoToken_whenDeleted_thenReturns401() {
      var response =
          asUnauthenticated().delete(resourcePath(), UUID.randomUUID()).then().extract().response();

      assertThat(response).hasStatus(UNAUTHORIZED.value());
    }

    @Test
    void givenUserRole_whenDeleted_thenReturns403() {
      var id = createAsAdmin(fullCreateBody());

      var response = asUser().delete(resourcePath(), id).then().extract().response();

      assertThat(response).hasStatus(FORBIDDEN.value()).hasCode(ErrorCode.FORBIDDEN.key());
    }

    @Test
    void givenUnknownId_whenDeleted_thenReturns404() {
      var response =
          asAdmin().delete(resourcePath(), UUID.randomUUID()).then().extract().response();

      assertThat(response).hasStatus(NOT_FOUND.value()).hasCode(notFoundCode().key());
    }

    @Test
    void givenInvalidUuid_whenDeleted_thenReturns400() {
      var response = asAdmin().delete(path() + "/not-a-uuid").then().extract().response();

      assertThat(response).hasStatus(BAD_REQUEST.value());
    }
  }
}
