package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.ChildEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ChildRepository;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ChildSpecifications;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChildService extends AbstractCrudService<ChildEntity, Child> {

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
  protected ErrorCode notFoundCode() {
    return ErrorCode.CHILD_NOT_FOUND;
  }

  @Transactional(readOnly = true)
  public Page<Child> search(List<UUID> ids, List<UUID> parentIds, String value, Pageable pageable) {
    return repository
        .findAll(ChildSpecifications.matches(ids, parentIds, value), pageable)
        .map(mapper::toModel);
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
