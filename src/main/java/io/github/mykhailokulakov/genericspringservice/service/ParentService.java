package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.ParentEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ParentSpecifications;
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
public class ParentService {

  private final ParentRepository repository;
  private final ParentEntityMapper mapper;

  public Parent create(Parent toCreate) {
    return mapper.toModel(repository.save(mapper.toEntity(toCreate)));
  }

  @Transactional(readOnly = true)
  public Parent getById(UUID id) {
    return mapper.toModel(findOrThrow(id));
  }

  @Transactional(readOnly = true)
  public Page<Parent> search(ParentFilter filter, Pageable pageable) {
    return repository.findAll(ParentSpecifications.matches(filter), pageable).map(mapper::toModel);
  }

  public Parent replace(UUID id, Long expectedVersion, Parent replacement) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyReplacement(replacement, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public Parent patch(UUID id, Long expectedVersion, ParentPatch patch) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyPatch(patch, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public void softDelete(UUID id) {
    repository.delete(findOrThrow(id));
  }

  private ParentEntity findOrThrow(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.PARENT_NOT_FOUND, id));
  }

  private ParentEntity loadAndCheckVersion(UUID id, Long expectedVersion) {
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
