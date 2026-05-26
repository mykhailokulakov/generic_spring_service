package io.github.mykhailokulakov.genericspringservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerPatch;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.OwnerEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.OwnerRepository;
import java.util.Optional;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

  @Mock private OwnerRepository repository;
  @Mock private ExampleRepository exampleRepository;
  @Mock private OwnerEntityMapper mapper;

  @InjectMocks private OwnerService service;

  private UUID id;
  private OwnerEntity entity;
  private Owner model;
  private ExampleEntity exampleEntity;

  @BeforeEach
  void setUp() {
    id = UUID.randomUUID();
    entity = OwnerEntity.builder().handle("h").version(3L).build();
    model = Instancio.create(Owner.class);
    exampleEntity =
        ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).version(1L).build();
  }

  @Test
  void create_savesAndReturnsModel() {
    when(mapper.toEntity(model)).thenReturn(entity);
    when(exampleRepository.findById(model.exampleId())).thenReturn(Optional.of(exampleEntity));
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model);

    var result = service.create(model);

    assertThat(result).isSameAs(model);
    verify(repository).save(entity);
  }

  @Test
  void create_throwsNotFoundWhenExampleMissing() {
    when(mapper.toEntity(model)).thenReturn(entity);
    when(exampleRepository.findById(model.exampleId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(model)).isInstanceOf(NotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void getById_returnsModelWhenFound() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(mapper.toModel(entity)).thenReturn(model);

    assertThat(service.getById(id)).isSameAs(model);
  }

  @Test
  void getById_throwsNotFoundWhenMissing() {
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getById(id)).isInstanceOf(NotFoundException.class);
  }

  @Test
  void replace_appliesReplacementOnMatchingVersion() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.saveAndFlush(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model);

    var result = service.replace(id, 3L, model);

    assertThat(result).isSameAs(model);
    verify(mapper).applyReplacement(model, entity);
    verify(repository).saveAndFlush(entity);
  }

  @Test
  void replace_throwsNotFoundWhenMissing() {
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.replace(id, 3L, model)).isInstanceOf(NotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void replace_throwsIfMatchRequiredWhenVersionNull() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service.replace(id, null, model))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.IF_MATCH_REQUIRED.key());
    verify(repository, never()).save(any());
  }

  @Test
  void replace_throwsNotFoundBeforeIfMatchWhenMissingAndVersionNull() {
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.replace(id, null, model))
        .isInstanceOf(NotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void replace_throwsConflictOnVersionMismatch() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service.replace(id, 7L, model)).isInstanceOf(ConflictException.class);
    verify(mapper, never()).applyReplacement(any(), any());
    verify(repository, never()).save(any());
  }

  @Test
  void patch_appliesPatchOnMatchingVersion() {
    var patch = Instancio.create(OwnerPatch.class);
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.saveAndFlush(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model);

    var result = service.patch(id, 3L, patch);

    assertThat(result).isSameAs(model);
    verify(mapper).applyPatch(patch, entity);
    verify(repository).saveAndFlush(entity);
  }

  @Test
  void patch_throwsNotFoundWhenMissing() {
    var patch = Instancio.create(OwnerPatch.class);
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.patch(id, 3L, patch)).isInstanceOf(NotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void patch_throwsConflictOnVersionMismatch() {
    var patch = Instancio.create(OwnerPatch.class);
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service.patch(id, 2L, patch)).isInstanceOf(ConflictException.class);
    verify(mapper, never()).applyPatch(any(), any());
    verify(repository, never()).save(any());
  }

  @Test
  void patch_throwsIfMatchRequiredWhenVersionNull() {
    var patch = Instancio.create(OwnerPatch.class);
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service.patch(id, null, patch))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.IF_MATCH_REQUIRED.key());
    verify(repository, never()).save(any());
  }

  @Test
  void patch_throwsNotFoundBeforeIfMatchWhenMissingAndVersionNull() {
    var patch = Instancio.create(OwnerPatch.class);
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.patch(id, null, patch)).isInstanceOf(NotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void softDelete_deletesWhenFound() {
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    service.softDelete(id);

    verify(repository, times(1)).delete(entity);
  }

  @Test
  void softDelete_throwsNotFoundWhenMissing() {
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.softDelete(id)).isInstanceOf(NotFoundException.class);
    verify(repository, never()).delete(any(OwnerEntity.class));
  }
}
