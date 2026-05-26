package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Left;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.mapper.LeftEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.LeftRepository;
import io.github.mykhailokulakov.genericspringservice.repository.specification.LeftSpecifications;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeftService extends AbstractCrudService<LeftEntity, Left> {

  private final LeftRepository repository;
  private final LeftEntityMapper mapper;

  @Override
  protected LeftRepository repository() {
    return repository;
  }

  @Override
  protected LeftEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected LeftEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.LEFT_NOT_FOUND;
  }

  @Transactional(readOnly = true)
  public Page<Left> search(List<UUID> ids, String code, Pageable pageable) {
    return repository.findAll(LeftSpecifications.matches(ids, code), pageable).map(mapper::toModel);
  }
}
