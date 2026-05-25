package io.github.mykhailokulakov.testentities.contract;

import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.testentities.OwnerEntity;

class OwnerEntityRepositoryIT extends AbstractRepositoryTestContract<OwnerEntity> {

  @Override
  protected void mutate(OwnerEntity entity) {
    entity.setHandle("mutated");
  }
}
