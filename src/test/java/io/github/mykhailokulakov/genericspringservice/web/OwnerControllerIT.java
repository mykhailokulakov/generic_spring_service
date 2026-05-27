package io.github.mykhailokulakov.genericspringservice.web;

import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asAdmin;

import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudControllerTestContract;
import io.restassured.http.ContentType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

class OwnerControllerIT extends AbstractCrudControllerTestContract<OwnerEntity, Owner> {

  private UUID exampleId;

  @Override
  protected String path() {
    return "/api/v1/owners";
  }

  @Override
  protected Class<OwnerEntity> entityClass() {
    return OwnerEntity.class;
  }

  @Override
  protected String notFoundCode() {
    return "error.owner.not-found";
  }

  @Override
  protected void setUpDependencies() {
    exampleId =
        UUID.fromString(
            asAdmin()
                .contentType(ContentType.JSON)
                .body(Map.of("name", "Test Example", "status", "ACTIVE"))
                .post("/api/v1/examples")
                .then()
                .statusCode(201)
                .extract()
                .response()
                .jsonPath()
                .getString("id"));
  }

  @Override
  protected Map<String, Object> fullCreateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("handle", "Owner A");
    body.put("exampleId", exampleId.toString());
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
}
