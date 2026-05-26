package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.EntityMapper;
import io.github.mykhailokulakov.genericspringservice.mapper.PatchableMapper;
import io.github.mykhailokulakov.genericspringservice.repository.FilterableRepository;
import io.github.mykhailokulakov.genericspringservice.service.AbstractCrudService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class AbstractCrudServiceTestContract<
    E extends SoftDeletable, M extends DomainModel, P> {

  protected UUID id;
  protected E entity;
  protected M model;
  protected P patch;

  protected abstract AbstractCrudService<E, M, P> service();

  protected abstract FilterableRepository<E> repository();

  protected abstract EntityMapper<E, M> mapper();

  protected abstract PatchableMapper<E, P> patchMapper();

  protected abstract E newEntity();

  protected abstract M newModel();

  protected abstract P newPatch();

  protected void setUpAdditionalCreateMocks() {}

  @BeforeEach
  void setUpContract() {
    id = UUID.randomUUID();
    entity = newEntity();
    model = newModel();
    patch = newPatch();
  }

  @Test
  void create_savesAndReturnsModel() {
    when(mapper().toEntity(model)).thenReturn(entity);
    setUpAdditionalCreateMocks();
    when(repository().save(entity)).thenReturn(entity);
    when(mapper().toModel(entity)).thenReturn(model);

    var result = service().create(model);

    assertThat(result).isSameAs(model);
    verify(repository()).save(entity);
  }

  @Test
  void getById_returnsModelWhenFound() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));
    when(mapper().toModel(entity)).thenReturn(model);

    assertThat(service().getById(id)).isSameAs(model);
  }

  @Test
  void getById_throwsNotFoundWhenMissing() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().getById(id)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void replace_appliesReplacementOnMatchingVersion() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));
    when(repository().saveAndFlush(entity)).thenReturn(entity);
    when(mapper().toModel(entity)).thenReturn(model);

    var result = service().replace(id, 3L, model);

    assertThat(result).isSameAs(model);
    verify(mapper()).applyReplacement(model, entity);
    verify(repository()).saveAndFlush(entity);
  }

  @Test
  void replace_throwsNotFoundWhenMissing() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().replace(id, 3L, model))
        .isInstanceOf(NotFoundException.class);
    verify(repository(), never()).save(any());
  }

  @Test
  void replace_throwsConflictOnVersionMismatch() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service().replace(id, 7L, model))
        .isInstanceOf(ConflictException.class);
    verify(mapper(), never()).applyReplacement(any(), any());
    verify(repository(), never()).save(any());
  }

  @Test
  void replace_throwsIfMatchRequiredWhenVersionNull() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service().replace(id, null, model))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.IF_MATCH_REQUIRED.key());
    verify(repository(), never()).save(any());
  }

  @Test
  void replace_throwsNotFoundBeforeIfMatchWhenMissingAndVersionNull() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().replace(id, null, model))
        .isInstanceOf(NotFoundException.class);
    verify(repository(), never()).save(any());
  }

  @Test
  void patch_appliesPatchOnMatchingVersion() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));
    when(repository().saveAndFlush(entity)).thenReturn(entity);
    when(mapper().toModel(entity)).thenReturn(model);

    var result = service().patch(id, 3L, patch);

    assertThat(result).isSameAs(model);
    verify(patchMapper()).applyPatch(patch, entity);
    verify(repository()).saveAndFlush(entity);
  }

  @Test
  void patch_throwsNotFoundWhenMissing() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().patch(id, 3L, patch)).isInstanceOf(NotFoundException.class);
    verify(repository(), never()).save(any());
  }

  @Test
  void patch_throwsConflictOnVersionMismatch() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service().patch(id, 2L, patch)).isInstanceOf(ConflictException.class);
    verify(patchMapper(), never()).applyPatch(any(), any());
    verify(repository(), never()).save(any());
  }

  @Test
  void patch_throwsIfMatchRequiredWhenVersionNull() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service().patch(id, null, patch))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.IF_MATCH_REQUIRED.key());
    verify(repository(), never()).save(any());
  }

  @Test
  void patch_throwsNotFoundBeforeIfMatchWhenMissingAndVersionNull() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().patch(id, null, patch))
        .isInstanceOf(NotFoundException.class);
    verify(repository(), never()).save(any());
  }

  @Test
  void softDelete_deletesWhenFound() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    service().softDelete(id);

    verify(repository(), times(1)).delete(entity);
  }

  @Test
  void softDelete_throwsNotFoundWhenMissing() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().softDelete(id)).isInstanceOf(NotFoundException.class);
    verify(repository(), never()).deleteById(any());
  }
}
