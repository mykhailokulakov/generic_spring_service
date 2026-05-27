package io.github.mykhailokulakov.genericspringservice.web;

import static io.github.mykhailokulakov.genericspringservice.support.auth.RestAssuredAuth.asAdmin;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudControllerTestContract;
import io.restassured.http.ContentType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

class ChildControllerIT extends AbstractCrudControllerTestContract<ChildEntity, Child> {

  private UUID parentId;

  @Override
  protected String path() {
    return "/api/v1/children";
  }

  @Override
  protected Class<ChildEntity> entityClass() {
    return ChildEntity.class;
  }

  @Override
  protected String notFoundCode() {
    return "error.child.not-found";
  }

  @Override
  protected void setUpDependencies() {
    parentId =
        UUID.fromString(
            asAdmin()
                .contentType(ContentType.JSON)
                .body(Map.of("label", "Test Parent"))
                .post("/api/v1/parents")
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
    body.put("value", "Child A");
    body.put("parentId", parentId.toString());
    return body;
  }

  @Override
  protected Map<String, Object> fullUpdateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("value", "Child B");
    return body;
  }

  @Override
  protected String requiredFieldName() {
    return "value";
  }

  @Override
  protected int requiredFieldMaxLength() {
    return 200;
  }

  @Override
  protected String patchFieldName() {
    return "value";
  }

  @Override
  protected Object patchFieldValue() {
    return "patched";
  }

  @Override
  protected Object patchFieldOriginalValue() {
    return "Child A";
  }
}
