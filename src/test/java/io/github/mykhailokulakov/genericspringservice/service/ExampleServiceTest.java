package io.github.mykhailokulakov.genericspringservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.exception.ConflictException;
import io.github.mykhailokulakov.genericspringservice.exception.ErrorCode;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.ExampleEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchExampleRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {

  @Mock private ExampleRepository repository;
  @Mock private ExampleEntityMapper mapper;

  @InjectMocks private ExampleService service;

  private UUID id;
  private ExampleEntity entity;
  private Example model;

  @BeforeEach
  void setUp() {
    id = UUID.randomUUID();
    entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).version(3L).build();
    model =
        new Example(
            id,
            "e",
            "d",
            1,
            new BigDecimal("9.99"),
            Instant.parse("2026-05-20T00:00:00Z"),
            ExampleStatus.DRAFT,
            Set.of("a"),
            Instant.parse("2026-05-19T00:00:00Z"),
            Instant.parse("2026-05-19T00:00:00Z"),
            3L);
  }

  @Test
  void create_savesAndReturnsModel() {
    when(mapper.toEntity(model)).thenReturn(entity);
    when(repository.save(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model);

    Example result = service.create(model);

    assertThat(result).isSameAs(model);
    verify(repository).save(entity);
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

    Example result = service.replace(id, 3L, model);

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
    assertThatThrownBy(() -> service.replace(id, null, model))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.IF_MATCH_REQUIRED.key());
    verify(repository, never()).findById(any(UUID.class));
    verify(repository, never()).save(any());
  }

  @Test
  void patch_throwsIfMatchRequiredWhenVersionNull() {
    PatchExampleRequest patch = new PatchExampleRequest(null, null, null, null, null, null, null);
    assertThatThrownBy(() -> service.patch(id, null, patch))
        .isInstanceOf(ConflictException.class)
        .hasMessage(ErrorCode.IF_MATCH_REQUIRED.key());
    verify(repository, never()).findById(any(UUID.class));
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
    PatchExampleRequest patch = new PatchExampleRequest("new", null, null, null, null, null, null);
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.saveAndFlush(entity)).thenReturn(entity);
    when(mapper.toModel(entity)).thenReturn(model);

    Example result = service.patch(id, 3L, patch);

    assertThat(result).isSameAs(model);
    verify(mapper).applyPatch(patch, entity);
    verify(repository).saveAndFlush(entity);
  }

  @Test
  void patch_throwsNotFoundWhenMissing() {
    PatchExampleRequest patch = new PatchExampleRequest(null, null, null, null, null, null, null);
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.patch(id, 3L, patch)).isInstanceOf(NotFoundException.class);
    verify(repository, never()).save(any());
  }

  @Test
  void patch_throwsConflictOnVersionMismatch() {
    PatchExampleRequest patch = new PatchExampleRequest(null, null, null, null, null, null, null);
    when(repository.findById(id)).thenReturn(Optional.of(entity));

    assertThatThrownBy(() -> service.patch(id, 2L, patch)).isInstanceOf(ConflictException.class);
    verify(mapper, never()).applyPatch(any(), any());
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
    verify(repository, never()).delete(any(ExampleEntity.class));
  }
}
