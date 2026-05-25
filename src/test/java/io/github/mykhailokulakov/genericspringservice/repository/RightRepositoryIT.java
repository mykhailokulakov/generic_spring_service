package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;

class RightRepositoryIT extends AbstractRepositoryTestContract<RightEntity> {

  @Override
  protected void mutate(RightEntity entity) {
    entity.setName("mutated");
  }
}
