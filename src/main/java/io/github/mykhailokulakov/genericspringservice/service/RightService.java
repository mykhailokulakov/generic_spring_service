package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.mapper.RightEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.RightRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.RightSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RightService extends AbstractCrudService<RightEntity, Right, RightPatch, RightFilter> {

  private final RightRepository repository;
  private final RightEntityMapper mapper;

  @Override
  protected RightRepository repository() {
    return repository;
  }

  @Override
  protected RightEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected RightEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected Specification<RightEntity> toSpecification(RightFilter filter) {
    return RightSpecifications.matches(filter);
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.RIGHT_NOT_FOUND;
  }
}
