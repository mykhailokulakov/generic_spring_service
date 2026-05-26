package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.mapper.ParentEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ParentSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParentService
    extends AbstractCrudService<ParentEntity, Parent, ParentPatch, ParentFilter> {

  private final ParentRepository repository;
  private final ParentEntityMapper mapper;

  @Override
  protected ParentRepository repository() {
    return repository;
  }

  @Override
  protected ParentEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected ParentEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected Specification<ParentEntity> toSpecification(ParentFilter filter) {
    return ParentSpecifications.matches(filter);
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.PARENT_NOT_FOUND;
  }
}
