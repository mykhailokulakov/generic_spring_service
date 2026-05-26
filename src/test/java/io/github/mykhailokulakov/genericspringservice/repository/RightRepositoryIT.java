package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import org.instancio.Instancio;

class RightRepositoryIT extends AbstractRepositoryTestContract<RightEntity> {

  @Override
  protected void mutate(RightEntity entity) {
    entity.setName(Instancio.create(String.class));
  }
}
