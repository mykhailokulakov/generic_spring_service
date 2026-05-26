package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import org.springframework.beans.factory.annotation.Autowired;

class OwnerRepositoryIT extends AbstractRepositoryTestContract<OwnerEntity> {

  @Autowired private ExampleRepository exampleRepository;

  @Override
  protected OwnerEntity newEntity() {
    var example = exampleRepository.saveAndFlush(RandomEntities.create(ExampleEntity.class));
    var owner = RandomEntities.create(OwnerEntity.class);
    owner.setExample(example);
    return owner;
  }
}
