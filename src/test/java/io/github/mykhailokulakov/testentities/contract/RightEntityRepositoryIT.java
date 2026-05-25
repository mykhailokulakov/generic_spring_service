package io.github.mykhailokulakov.testentities.contract;

import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.testentities.RightEntity;

class RightEntityRepositoryIT extends AbstractRepositoryTestContract<RightEntity> {

  @Override
  protected void mutate(RightEntity entity) {
    entity.setName("mutated");
  }
}
