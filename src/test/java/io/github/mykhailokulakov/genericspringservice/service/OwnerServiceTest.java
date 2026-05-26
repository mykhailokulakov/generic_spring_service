package io.github.mykhailokulakov.genericspringservice.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerPatch;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.OwnerEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.repository.OwnerRepository;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudServiceTestContract;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest extends AbstractCrudServiceTestContract<OwnerEntity, Owner, OwnerPatch> {

  @Mock private OwnerRepository repository;
  @Mock private ExampleRepository exampleRepository;
  @Mock private OwnerEntityMapper mapper;

  @InjectMocks private OwnerService service;

  @Override
  protected OwnerService service() {
    return service;
  }

  @Override
  protected OwnerRepository repository() {
    return repository;
  }

  @Override
  protected OwnerEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected OwnerEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected OwnerEntity newEntity() {
    return OwnerEntity.builder().handle("h").version(3L).build();
  }

  @Override
  protected Owner newModel() {
    return Instancio.create(Owner.class);
  }

  @Override
  protected OwnerPatch newPatch() {
    return Instancio.create(OwnerPatch.class);
  }

  @Override
  protected void setUpAdditionalCreateMocks() {
    var exampleEntity =
        ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).version(1L).build();
    when(exampleRepository.findById(model.exampleId())).thenReturn(Optional.of(exampleEntity));
  }

  @Test
  void create_throwsNotFoundWhenExampleMissing() {
    var toCreate = Instancio.create(Owner.class);
    when(mapper.toEntity(toCreate)).thenReturn(newEntity());
    when(exampleRepository.findById(toCreate.exampleId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(toCreate)).isInstanceOf(NotFoundException.class);
  }
}
