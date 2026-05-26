package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Left;
import io.github.mykhailokulakov.genericspringservice.domain.model.LeftFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.LeftPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.mapper.LeftEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.LeftRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.LeftSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeftService extends AbstractCrudService<LeftEntity, Left, LeftPatch, LeftFilter> {

  private final LeftRepository repository;
  private final LeftEntityMapper mapper;

  @Override
  protected LeftRepository repository() {
    return repository;
  }

  @Override
  protected LeftEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected LeftEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected Specification<LeftEntity> toSpecification(LeftFilter filter) {
    return LeftSpecifications.matches(filter);
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.LEFT_NOT_FOUND;
  }
}
