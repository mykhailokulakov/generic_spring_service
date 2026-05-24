package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleFilter;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExamplePatch;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.ExampleEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.ExampleSpecifications;
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
public class ExampleService {

  private final ExampleRepository repository;
  private final ExampleEntityMapper mapper;

  public Example create(Example toCreate) {
    return mapper.toModel(repository.save(mapper.toEntity(toCreate)));
  }

  @Transactional(readOnly = true)
  public Example getById(UUID id) {
    return repository
        .findById(id)
        .map(mapper::toModel)
        .orElseThrow(() -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, id));
  }

  @Transactional(readOnly = true)
  public Page<Example> search(ExampleFilter filter, Pageable pageable) {
    return repository.findAll(ExampleSpecifications.matches(filter), pageable).map(mapper::toModel);
  }

  public Example replace(UUID id, Long expectedVersion, Example replacement) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyReplacement(replacement, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public Example patch(UUID id, Long expectedVersion, ExamplePatch patch) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyPatch(patch, entity);
    return mapper.toModel(repository.saveAndFlush(entity));
  }

  public void softDelete(UUID id) {
    var entity =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, id));
    repository.delete(entity);
  }

  private ExampleEntity loadAndCheckVersion(UUID id, Long expectedVersion) {
    if (expectedVersion == null) {
      throw new ConflictException(ErrorCode.IF_MATCH_REQUIRED, id);
    }
    var entity =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, id));
    if (!Objects.equals(entity.getVersion(), expectedVersion)) {
      throw new ConflictException(ErrorCode.OPTIMISTIC_LOCK, id);
    }
    return entity;
  }
}
