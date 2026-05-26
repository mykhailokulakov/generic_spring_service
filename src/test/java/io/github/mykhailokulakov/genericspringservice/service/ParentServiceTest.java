package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.mapper.ParentEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ParentRepository;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudServiceTestContract;
import org.instancio.Instancio;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ParentServiceTest extends AbstractCrudServiceTestContract<ParentEntity, Parent> {

  @Mock private ParentRepository repository;
  @Mock private ParentEntityMapper mapper;

  @InjectMocks private ParentService service;

  @Override
  protected ParentService service() {
    return service;
  }

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
  protected ParentEntity newEntity() {
    return ParentEntity.builder().label("p").version(3L).build();
  }

  @Override
  protected Parent newModel() {
    return Instancio.create(Parent.class);
  }

  @Override
  protected Parent newPatch() {
    return Instancio.create(Parent.class);
  }
}
