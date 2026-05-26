package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import org.instancio.Instancio;

class LeftRepositoryIT extends AbstractRepositoryTestContract<LeftEntity> {

  @Override
  protected void mutate(LeftEntity entity) {
    entity.setCode(Instancio.create(String.class));
  }
}
