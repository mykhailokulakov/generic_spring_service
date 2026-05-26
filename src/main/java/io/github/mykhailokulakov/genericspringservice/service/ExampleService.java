package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExamplePatch;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.mapper.ExampleEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ExampleSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExampleService
    extends AbstractCrudService<ExampleEntity, Example, ExamplePatch, ExampleFilter> {

  private final ExampleRepository repository;
  private final ExampleEntityMapper mapper;

  @Override
  protected ExampleRepository repository() {
    return repository;
  }

  @Override
  protected ExampleEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected ExampleEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected Specification<ExampleEntity> toSpecification(ExampleFilter filter) {
    return ExampleSpecifications.matches(filter);
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.EXAMPLE_NOT_FOUND;
  }
}
