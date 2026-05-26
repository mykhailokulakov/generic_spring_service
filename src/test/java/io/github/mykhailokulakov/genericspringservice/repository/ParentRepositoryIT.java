package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import org.instancio.Instancio;

class ParentRepositoryIT extends AbstractRepositoryTestContract<ParentEntity> {

  @Override
  protected void mutate(ParentEntity entity) {
    entity.setLabel(Instancio.create(String.class));
  }
}
