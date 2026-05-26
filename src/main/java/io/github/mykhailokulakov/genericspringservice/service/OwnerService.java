package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.OwnerEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.OwnerRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.OwnerSpecifications;
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
public class OwnerService {

  private final OwnerRepository repository;
  private final ExampleRepository exampleRepository;
  private final OwnerEntityMapper mapper;

  public Owner create(Owner toCreate) {
    var entity = mapper.toEntity(toCreate);
    entity.setExample(
        exampleRepository
            .findById(toCreate.exampleId())
            .orElseThrow(
                () -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, toCreate.exampleId())));
    return mapper.toModel(repository.save(entity));
  }

  @Transactional(readOnly = true)
  public Owner getById(UUID id) {
    return mapper.toModel(findOrThrow(id));
  }

  @Transactional(readOnly = true)
  public Page<Owner> search(OwnerFilter filter, Pageable pageable) {
    return repository.findAll(OwnerSpecifications.matches(filter), pageable).map(mapper::toModel);
  }

  public Owner replace(UUID id, Long expectedVersion, Owner replacement) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyReplacement(replacement, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public Owner patch(UUID id, Long expectedVersion, OwnerPatch patch) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyPatch(patch, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public void softDelete(UUID id) {
    repository.delete(findOrThrow(id));
  }

  private OwnerEntity findOrThrow(UUID id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.OWNER_NOT_FOUND, id));
  }

  private OwnerEntity loadAndCheckVersion(UUID id, Long expectedVersion) {
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
