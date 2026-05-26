package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.EntityMapper;
import io.github.mykhailokulakov.genericspringservice.mapper.PatchableMapper;
import io.github.mykhailokulakov.genericspringservice.repository.FilterableRepository;
import java.util.Objects;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public abstract class AbstractCrudService<E extends SoftDeletable, M extends DomainModel, P> {

  protected abstract FilterableRepository<E> repository();

  protected abstract EntityMapper<E, M> mapper();

  protected abstract PatchableMapper<E, P> patchMapper();

  protected abstract ErrorCode notFoundCode();

  public M create(M toCreate) {
    return mapper().toModel(repository().save(mapper().toEntity(toCreate)));
  }

  @Transactional(readOnly = true)
  public M getById(UUID id) {
    return mapper().toModel(findOrThrow(id));
  }

  public M replace(UUID id, Long expectedVersion, M replacement) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    mapper().applyReplacement(replacement, entity);
    return mapper().toModel(repository().saveAndFlush(entity));
  }

  public M patch(UUID id, Long expectedVersion, P patch) {
    var entity = loadAndCheckVersion(id, expectedVersion);
    patchMapper().applyPatch(patch, entity);
    return mapper().toModel(repository().saveAndFlush(entity));
  }

  public void softDelete(UUID id) {
    repository().delete(findOrThrow(id));
  }

  protected E findOrThrow(UUID id) {
    return repository().findById(id).orElseThrow(() -> new NotFoundException(notFoundCode(), id));
  }

  private E loadAndCheckVersion(UUID id, Long expectedVersion) {
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
