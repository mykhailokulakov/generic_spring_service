package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;

class ExampleRepositoryIT extends AbstractRepositoryTestContract<ExampleEntity> {

  @Override
  protected void mutate(ExampleEntity entity) {
    entity.setName("mutated");
  }
}
