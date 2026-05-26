package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.ChildEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ChildRepository;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ChildSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChildService extends AbstractCrudService<ChildEntity, Child, ChildPatch, ChildFilter> {

  private final ChildRepository repository;
  private final ParentRepository parentRepository;
  private final ChildEntityMapper mapper;

  @Override
  protected ChildRepository repository() {
    return repository;
  }

  @Override
  protected ChildEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected ChildEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected Specification<ChildEntity> toSpecification(ChildFilter filter) {
    return ChildSpecifications.matches(filter);
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.CHILD_NOT_FOUND;
  }

  @Override
  public Child create(Child toCreate) {
    var entity = mapper.toEntity(toCreate);
    entity.setParent(
        parentRepository
            .findById(toCreate.parentId())
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.PARENT_NOT_FOUND, toCreate.parentId())));
    return mapper.toModel(repository.save(entity));
  }
}
