package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.RightEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.RightRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.RightSpecifications;
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
public class RightService {

  private final RightRepository repository;
  private final RightEntityMapper mapper;

  public Right create(Right toCreate) {
    return mapper.toModel(repository.save(mapper.toEntity(toCreate)));
  }

  @Transactional(readOnly = true)
  public Right getById(UUID id) {
    return mapper.toModel(findOrThrow(id));
  }

  @Transactional(readOnly = true)
  public Page<Right> search(RightFilter filter, Pageable pageable) {
    return repository.findAll(RightSpecifications.matches(filter), pageable).map(mapper::toModel);
  }

  public Right replace(UUID id, Long expectedVersion, Right replacement) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyReplacement(replacement, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public Right patch(UUID id, Long expectedVersion, RightPatch patch) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyPatch(patch, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public void softDelete(UUID id) {
    repository.delete(findOrThrow(id));
  }

  private RightEntity findOrThrow(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.RIGHT_NOT_FOUND, id));
  }

  private RightEntity loadAndCheckVersion(UUID id, Long expectedVersion) {
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
