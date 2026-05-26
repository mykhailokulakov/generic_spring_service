package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.mapper.ExampleEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudServiceTestContract;
import org.instancio.Instancio;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExampleServiceTest extends AbstractCrudServiceTestContract<ExampleEntity, Example> {

  @Mock private ExampleRepository repository;
  @Mock private ExampleEntityMapper mapper;

  @InjectMocks private ExampleService service;

  @Override
  protected ExampleService service() {
    return service;
  }

  @Override
  protected ExampleRepository repository() {
    return repository;
  }

  @Override
  protected ExampleEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected ExampleEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected ExampleEntity newEntity() {
    return ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).version(3L).build();
  }

  @Override
  protected Example newModel() {
    return Instancio.create(Example.class);
  }

  @Override
  protected Example newPatch() {
    return Instancio.create(Example.class);
  }
}
