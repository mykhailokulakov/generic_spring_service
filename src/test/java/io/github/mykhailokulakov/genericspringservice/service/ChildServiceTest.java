package io.github.mykhailokulakov.genericspringservice.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.exception.NotFoundException;
import io.github.mykhailokulakov.genericspringservice.mapper.ChildEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ChildRepository;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudServiceTestContract;
import java.util.Optional;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChildServiceTest extends AbstractCrudServiceTestContract<ChildEntity, Child> {

  @Mock private ChildRepository repository;
  @Mock private ParentRepository parentRepository;
  @Mock private ChildEntityMapper mapper;

  @InjectMocks private ChildService service;

  @Override
  protected ChildService service() {
    return service;
  }

  @Override
  protected ChildRepository repository() {
    return repository;
  }

  @Override
  protected ChildEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected ChildEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected ChildEntity newEntity() {
    return ChildEntity.builder().value("c").version(3L).build();
  }

  @Override
  protected Child newModel() {
    return Instancio.create(Child.class);
  }

  @Override
  protected Child newPatch() {
    return Instancio.create(Child.class);
  }

  @Override
  protected void setUpAdditionalCreateMocks() {
    var parentEntity = ParentEntity.builder().label("p").version(1L).build();
    when(parentRepository.findById(model.parentId())).thenReturn(Optional.of(parentEntity));
  }

  @Test
  void givenMissingParent_whenCreated_thenThrowsNotFound() {
    var toCreate = Instancio.create(Child.class);
    when(mapper.toEntity(toCreate)).thenReturn(newEntity());
    when(parentRepository.findById(toCreate.parentId())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(toCreate)).isInstanceOf(NotFoundException.class);
  }
}
