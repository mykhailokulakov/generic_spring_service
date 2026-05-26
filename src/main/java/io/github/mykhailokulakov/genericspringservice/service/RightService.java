package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.mapper.RightEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.RightRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.RightSpecifications;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RightService extends AbstractCrudService<RightEntity, Right> {

  private final RightRepository repository;
  private final RightEntityMapper mapper;

  @Override
  protected RightRepository repository() {
    return repository;
  }

  @Override
  protected RightEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected RightEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.RIGHT_NOT_FOUND;
  }

  @Transactional(readOnly = true)
  public Page<Right> search(List<UUID> ids, String name, Pageable pageable) {
    return repository
        .findAll(RightSpecifications.matches(ids, name), pageable)
        .map(mapper::toModel);
  }
}
