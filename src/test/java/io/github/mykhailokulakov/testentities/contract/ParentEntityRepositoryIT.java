package io.github.mykhailokulakov.testentities.contract;

import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.testentities.ParentEntity;

class ParentEntityRepositoryIT extends AbstractRepositoryTestContract<ParentEntity> {

  @Override
  protected void mutate(ParentEntity entity) {
    entity.setLabel("mutated");
  }
}
