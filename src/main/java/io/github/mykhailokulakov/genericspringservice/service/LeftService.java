package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Left;
import io.github.mykhailokulakov.genericspringservice.domain.model.LeftFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.LeftPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.LeftEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.LeftRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.LeftSpecifications;
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
public class LeftService {

  private final LeftRepository repository;
  private final LeftEntityMapper mapper;

  public Left create(Left toCreate) {
    return mapper.toModel(repository.save(mapper.toEntity(toCreate)));
  }

  @Transactional(readOnly = true)
  public Left getById(UUID id) {
    return mapper.toModel(findOrThrow(id));
  }

  @Transactional(readOnly = true)
  public Page<Left> search(LeftFilter filter, Pageable pageable) {
    return repository.findAll(LeftSpecifications.matches(filter), pageable).map(mapper::toModel);
  }

  public Left replace(UUID id, Long expectedVersion, Left replacement) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyReplacement(replacement, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public Left patch(UUID id, Long expectedVersion, LeftPatch patch) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyPatch(patch, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public void softDelete(UUID id) {
    repository.delete(findOrThrow(id));
  }

  private LeftEntity findOrThrow(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.LEFT_NOT_FOUND, id));
  }

  private LeftEntity loadAndCheckVersion(UUID id, Long expectedVersion) {
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
