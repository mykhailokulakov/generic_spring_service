package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.ExampleEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchExampleRequest;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

  public Example replace(UUID id, Long expectedVersion, Example replacement) {
    ExampleEntity entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyReplacement(replacement, entity);
    return mapper.toModel(repository.save(entity));
  }

  public Example patch(UUID id, Long expectedVersion, PatchExampleRequest patch) {
    ExampleEntity entity = loadAndCheckVersion(id, expectedVersion);
    mapper.applyPatch(patch, entity);
    return mapper.toModel(repository.save(entity));
  }

  public void softDelete(UUID id) {
    ExampleEntity entity =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, id));
    repository.delete(entity);
  }

  private ExampleEntity loadAndCheckVersion(UUID id, Long expectedVersion) {
    ExampleEntity entity =
        repository
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorCode.EXAMPLE_NOT_FOUND, id));
    if (!Objects.equals(entity.getVersion(), expectedVersion)) {
      throw new ConflictException(ErrorCode.OPTIMISTIC_LOCK, id);
    }
    return entity;
  }
}
