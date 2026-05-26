package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import java.util.UUID;

class LeftRepositoryIT extends AbstractRepositoryTestContract<LeftEntity> {

  @Override
  protected void mutate(LeftEntity entity) {
    entity.setCode(UUID.randomUUID().toString());
  }
}
