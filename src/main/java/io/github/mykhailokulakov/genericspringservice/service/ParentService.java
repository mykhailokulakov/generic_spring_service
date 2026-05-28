package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.mapper.ParentEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
import io.github.mykhailokulakov.genericspringservice.repository.query.ParentQueries;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParentService extends AbstractCrudService<ParentEntity, Parent> {

  private final ParentRepository repository;
  private final ParentEntityMapper mapper;

  @Override
  protected ParentRepository repository() {
    return repository;
  }

  @Override
  protected ParentEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected ParentEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected ErrorCode notFoundCode() {
    return ErrorCode.PARENT_NOT_FOUND;
  }

  @Transactional(readOnly = true)
  public Page<Parent> search(List<UUID> ids, String label, Pageable pageable) {
    return repository.findAll(ParentQueries.matches(ids, label), pageable).map(mapper::toModel);
  }
}
