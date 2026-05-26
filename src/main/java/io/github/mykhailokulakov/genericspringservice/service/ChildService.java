package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.ChildEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ChildRepository;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ChildSpecifications;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ChildService {

  private final ChildRepository repository;
  private final ParentRepository parentRepository;
  private final ChildEntityMapper mapper;

  public Child create(Child toCreate) {
    var entity = mapper.toEntity(toCreate);
    entity.setParent(
        parentRepository
            .findById(toCreate.parentId())
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.PARENT_NOT_FOUND, toCreate.parentId())));
    return mapper.toModel(repository.save(entity));
  }

  @Transactional(readOnly = true)
  public Child getById(UUID id) {
    return mapper.toModel(findOrThrow(id));
  }

  @Transactional(readOnly = true)
  public Page<Child> search(ChildFilter filter, Pageable pageable) {
    return repository.findAll(ChildSpecifications.matches(filter), pageable).map(mapper::toModel);
  }

  public Child replace(UUID id, Long expectedVersion, Child replacement) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyReplacement(replacement, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public Child patch(UUID id, Long expectedVersion, ChildPatch patch) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyPatch(patch, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public void softDelete(UUID id) {
    repository.delete(findOrThrow(id));
  }

  private ChildEntity findOrThrow(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.CHILD_NOT_FOUND, id));
  }

  private ChildEntity loadAndCheckVersion(UUID id, Long expectedVersion) {
    var entity = findOrThrow(id);
    if (expectedVersion == null) {
      throw new ConflictException(ErrorCode.IF_MATCH_REQUIRED, id);
    }
    if (!Objects.equals(entity.getVersion(), expectedVersion)) {
      throw new ConflictException(ErrorCode.OPTIMISTIC_LOCK, id);
    }
    return entity;
  }
}
