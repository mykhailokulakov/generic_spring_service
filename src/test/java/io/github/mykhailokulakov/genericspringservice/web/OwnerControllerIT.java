package io.github.mykhailokulakov.genericspringservice.web;

import static io.github.mykhailokulakov.genericspringservice.support.assertions.Assertions.assertThat;
import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asAdmin;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudControllerTestContract;
import io.restassured.http.ContentType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OwnerControllerIT extends AbstractCrudControllerTestContract<OwnerEntity> {

  @Override
  protected String path() {
    return "/api/v1/owners";
  }

  @Override
  protected Class<OwnerEntity> entityClass() {
    return OwnerEntity.class;
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.OWNER_NOT_FOUND;
  }

  private UUID createExample() {
    return UUID.fromString(
        asAdmin()
            .contentType(ContentType.JSON)
            .body(Map.of("name", "Test Example", "status", "ACTIVE"))
            .post("/api/v1/examples")
            .then()
            .statusCode(CREATED.value())
            .extract()
            .response()
            .jsonPath()
            .getString("id"));
  }

  @Override
  protected Map<String, Object> fullCreateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("handle", "Owner A");
    body.put("exampleId", createExample().toString());
    return body;
  }

  @Override
  protected Map<String, Object> fullUpdateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("handle", "Owner B");
    return body;
  }

  @Override
  protected String requiredFieldName() {
    return "handle";
  }

  @Override
  protected int requiredFieldMaxLength() {
    return 200;
  }

  @Override
  protected String patchFieldName() {
    return "handle";
  }

  @Override
  protected Object patchFieldValue() {
    return "patched";
  }

  @Override
  protected Object patchFieldOriginalValue() {
    return "Owner A";
  }

  @Override
  protected String ukrainianNotFoundPrefix() {
    return "Власник";
  }

  @Nested
  class OwnerDeleteEndpoint {

    @BeforeEach
    void clean() {
      db.truncateAll();
    }

    @Test
    void givenOwnerLinkedToExampleSoftDeleted_whenRecreatedWithSameExample_thenSucceedsWithNewId() {
      var exampleId = createExample();
      var body = new LinkedHashMap<String, Object>();
      body.put("handle", "Owner A");
      body.put("exampleId", exampleId.toString());
      var first = createAsAdmin(body);
      asAdmin().delete(resourcePath(), first).then().statusCode(NO_CONTENT.value());

      var second = createAsAdmin(body);

      assertThat(second).isNotEqualTo(first);
    }
  }
}
