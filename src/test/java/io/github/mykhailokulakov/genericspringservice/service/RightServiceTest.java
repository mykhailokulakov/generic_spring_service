package io.github.mykhailokulakov.genericspringservice.service;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightPatch;
import io.github.mykhailokulakov.genericspringservice.mapper.RightEntityMapper;
import io.github.mykhailokulakov.genericspringservice.repository.RightRepository;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractCrudServiceTestContract;
import org.instancio.Instancio;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RightServiceTest extends AbstractCrudServiceTestContract<RightEntity, Right, RightPatch> {

  @Mock private RightRepository repository;
  @Mock private RightEntityMapper mapper;

  @InjectMocks private RightService service;

  @Override
  protected RightService service() {
    return service;
  }

  @Override
  protected RightRepository repository() {
    return repository;
  }

  @Override
  protected RightEntityMapper mapper() {
    return mapper;
  }

  @Override
  protected RightEntityMapper patchMapper() {
    return mapper;
  }

  @Override
  protected RightEntity newEntity() {
    return RightEntity.builder().name("r").version(3L).build();
  }

  @Override
  protected Right newModel() {
    return Instancio.create(Right.class);
  }

  @Override
  protected RightPatch newPatch() {
    return Instancio.create(RightPatch.class);
  }
}
