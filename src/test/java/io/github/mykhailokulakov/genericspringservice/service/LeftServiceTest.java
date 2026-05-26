package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Left;
import io.github.mykhailokulakov.genericspringservice.mapper.LeftEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.LeftRepository;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudServiceTestContract;
import org.instancio.Instancio;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LeftServiceTest extends AbstractCrudServiceTestContract<LeftEntity, Left> {

  @Mock private LeftRepository repository;
  @Mock private LeftEntityMapper mapper;

  @InjectMocks private LeftService service;

  @Override
  protected LeftService service() {
    return service;
  }

  @Override
  protected LeftRepository repository() {
    return repository;
  }

  @Override
  protected LeftEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected LeftEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected LeftEntity newEntity() {
    return LeftEntity.builder().code("l").version(3L).build();
  }

  @Override
  protected Left newModel() {
    return Instancio.create(Left.class);
  }

  @Override
  protected Left newPatch() {
    return Instancio.create(Left.class);
  }
}
