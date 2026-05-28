package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.mapper.ExampleEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.query.ExampleQueries;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExampleService extends AbstractCrudService<ExampleEntity, Example> {

  private final ExampleRepository repository;
  private final ExampleEntityMapper mapper;

  @Override
  protected ExampleRepository repository() {
    return repository;
  }

  @Override
  protected ExampleEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected ExampleEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.EXAMPLE_NOT_FOUND;
  }

  @Transactional(readOnly = true)
  public Page<Example> search(
      List<UUID> ids,
      String name,
      String description,
      Integer minQuantity,
      Integer maxQuantity,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Instant occurredFrom,
      Instant occurredTo,
      Set<ExampleStatus> statuses,
      Set<String> tags,
      Pageable pageable) {
    return repository
        .findAll(
            ExampleQueries.matches(
                ids,
                name,
                description,
                minQuantity,
                maxQuantity,
                minPrice,
                maxPrice,
                occurredFrom,
                occurredTo,
                statuses,
                tags),
            pageable)
        .map(mapper::toModel);
  }
}
