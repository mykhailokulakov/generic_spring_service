package io.github.mykhailokulakov.genericspringservice.web;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudControllerTestContract;
import java.util.LinkedHashMap;
import java.util.Map;

class ParentControllerIT extends AbstractCrudControllerTestContract<ParentEntity> {

  @Override
  protected String path() {
    return "/api/v1/parents";
  }

  @Override
  protected Class<ParentEntity> entityClass() {
    return ParentEntity.class;
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.PARENT_NOT_FOUND;
  }

  @Override
  protected Map<String, Object> fullCreateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("label", "Parent A");
    return body;
  }

  @Override
  protected Map<String, Object> fullUpdateBody() {
    var body = new LinkedHashMap<String, Object>();
    body.put("label", "Parent B");
    return body;
  }

  @Override
  protected String requiredFieldName() {
    return "label";
  }

  @Override
  protected int requiredFieldMaxLength() {
    return 200;
  }

  @Override
  protected String patchFieldName() {
    return "label";
  }

  @Override
  protected Object patchFieldValue() {
    return "patched";
  }

  @Override
  protected Object patchFieldOriginalValue() {
    return "Parent A";
  }

  @Override
  protected String ukrainianNotFoundPrefix() {
    return "Батьківський";
  }
}
