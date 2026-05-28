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
    E extends SoftDeletable, M extends DomainModel> {

  protected UUID id;
  protected E entity;
  protected M model;
  protected M patch;

  protected abstract AbstractCrudService<E, M> service();

  protected abstract FilterableRepository<E> repository();

  protected abstract EntityMapper<E, M> mapper();

  protected abstract PatchableMapper<E, M> patchMapper();

  protected abstract E newEntity();

  protected abstract M newModel();

  protected abstract M newPatch();

  protected void setUpAdditionalCreateMocks() {}

  @BeforeEach
  void setUpContract() {
    id = UUID.randomUUID();
    entity = newEntity();
    model = newModel();
    patch = newPatch();
  }

  @Test
  void givenValidModel_whenCreated_thenSavesEntityAndReturnsModel() {
    when(mapper().toEntity(model)).thenReturn(entity);
    setUpAdditionalCreateMocks();
    when(repository().save(entity)).thenReturn(entity);
    when(mapper().toModel(entity)).thenReturn(model);

    var result = service().create(model);

    assertThat(result).isSameAs(model);
    verify(repository()).save(entity);
  }

  @Test
  void givenExistingId_whenFetchedById_thenReturnsModel() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));
    when(mapper().toModel(entity)).thenReturn(model);

    var result = service().getById(id);

    assertThat(result).isSameAs(model);
  }

  @Test
  void givenMissingId_whenFetchedById_thenThrowsNotFound() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().getById(id)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void givenMatchingVersion_whenReplaced_thenReplacementIsAppliedAndPersisted() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));
    when(repository().saveAndFlush(entity)).thenReturn(entity);
    when(mapper().toModel(entity)).thenReturn(model);

    var result = service().replace(id, 3L, model);

    assertThat(result).isSameAs(model);
    verify(mapper()).applyReplacement(model, entity);
    verify(repository()).saveAndFlush(entity);
  }

  @Test
  void givenMissingId_whenReplaced_thenThrowsNotFoundAndNothingIsSaved() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().replace(id, 3L, model))
        .isInstanceOf(NotFoundException.class);
    verify(repository(), never()).save(any());
  }

  @Test
  void givenStaleVersion_whenReplaced_thenThrowsConflictAndNothingIsSaved() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service().replace(id, 7L, model))
        .isInstanceOf(ConflictException.class);
    verify(mapper(), never()).applyReplacement(any(), any());
    verify(repository(), never()).save(any());
  }

  @Test
  void givenNullVersionOnExistingEntity_whenReplaced_thenThrowsIfMatchRequired() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service().replace(id, null, model))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.IF_MATCH_REQUIRED.key());
    verify(repository(), never()).save(any());
  }

  @Test
  void givenMissingIdAndNullVersion_whenReplaced_thenThrowsNotFoundBeforeIfMatchCheck() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().replace(id, null, model))
        .isInstanceOf(NotFoundException.class);
    verify(repository(), never()).save(any());
  }

  @Test
  void givenMatchingVersion_whenPatched_thenPatchIsAppliedAndPersisted() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));
    when(repository().saveAndFlush(entity)).thenReturn(entity);
    when(mapper().toModel(entity)).thenReturn(model);

    var result = service().patch(id, 3L, patch);

    assertThat(result).isSameAs(model);
    verify(patchMapper()).applyPatch(patch, entity);
    verify(repository()).saveAndFlush(entity);
  }

  @Test
  void givenMissingId_whenPatched_thenThrowsNotFoundAndNothingIsSaved() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().patch(id, 3L, patch)).isInstanceOf(NotFoundException.class);
    verify(repository(), never()).save(any());
  }

  @Test
  void givenStaleVersion_whenPatched_thenThrowsConflictAndNothingIsSaved() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service().patch(id, 2L, patch)).isInstanceOf(ConflictException.class);
    verify(patchMapper(), never()).applyPatch(any(), any());
    verify(repository(), never()).save(any());
  }

  @Test
  void givenNullVersionOnExistingEntity_whenPatched_thenThrowsIfMatchRequired() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service().patch(id, null, patch))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.IF_MATCH_REQUIRED.key());
    verify(repository(), never()).save(any());
  }

  @Test
  void givenMissingIdAndNullVersion_whenPatched_thenThrowsNotFoundBeforeIfMatchCheck() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().patch(id, null, patch))
        .isInstanceOf(NotFoundException.class);
    verify(repository(), never()).save(any());
  }

  @Test
  void givenExistingId_whenSoftDeleted_thenRepositoryDeleteIsInvoked() {
    when(repository().findById(id)).thenReturn(Optional.of(entity));

    service().softDelete(id);

    verify(repository(), times(1)).delete(entity);
  }

  @Test
  void givenMissingId_whenSoftDeleted_thenThrowsNotFoundAndNothingIsDeleted() {
    when(repository().findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service().softDelete(id)).isInstanceOf(NotFoundException.class);
    verify(repository(), never()).deleteById(any());
  }
}
