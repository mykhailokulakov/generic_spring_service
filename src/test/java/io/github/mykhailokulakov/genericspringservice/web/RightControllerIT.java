package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudControllerTestContract;
import java.util.LinkedHashMap;
import java.util.Map;

class RightControllerIT extends AbstractCrudControllerTestContract<RightEntity> {

  @Override
  protected String path() {
    return "/api/v1/rights";
  }

  @Override
  protected Class<RightEntity> entityClass() {
    return RightEntity.class;
  }

  @Override
  protected String notFoundCode() {
    return "error.right.not-found";
  }

  @Override
  protected Map<String, Object> fullCreateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("name", "Right A");
    return body;
  }

  @Override
  protected Map<String, Object> fullUpdateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("name", "Right B");
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
    return "patched";
  }

  @Override
  protected Object patchFieldOriginalValue() {
    return "Right A";
  }

  @Override
  protected String ukrainianNotFoundPrefix() {
    return "Правий";
  }
}
