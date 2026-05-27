package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudControllerTestContract;
import java.util.LinkedHashMap;
import java.util.Map;

class LeftControllerIT extends AbstractCrudControllerTestContract<LeftEntity> {

  @Override
  protected String path() {
    return "/api/v1/lefts";
  }

  @Override
  protected Class<LeftEntity> entityClass() {
    return LeftEntity.class;
  }

  @Override
  protected String notFoundCode() {
    return "error.left.not-found";
  }

  @Override
  protected Map<String, Object> fullCreateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("code", "LEFT-001");
    return body;
  }

  @Override
  protected Map<String, Object> fullUpdateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("code", "LEFT-002");
    return body;
  }

  @Override
  protected String requiredFieldName() {
    return "code";
  }

  @Override
  protected int requiredFieldMaxLength() {
    return 100;
  }

  @Override
  protected String patchFieldName() {
    return "code";
  }

  @Override
  protected Object patchFieldValue() {
    return "patched";
  }

  @Override
  protected Object patchFieldOriginalValue() {
    return "LEFT-001";
  }

  @Override
  protected String ukrainianNotFoundPrefix() {
    return "Лівий";
  }
}
