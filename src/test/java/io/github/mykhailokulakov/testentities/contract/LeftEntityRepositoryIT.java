package io.github.mykhailokulakov.testentities.contract;

import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.testentities.LeftEntity;

class LeftEntityRepositoryIT extends AbstractRepositoryTestContract<LeftEntity> {

  @Override
  protected void mutate(LeftEntity entity) {
    entity.setCode("mutated");
  }
}
