package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;

class ParentRepositoryIT extends AbstractRepositoryTestContract<ParentEntity> {

  @Override
  protected void mutate(ParentEntity entity) {
    entity.setLabel("mutated");
  }
}
