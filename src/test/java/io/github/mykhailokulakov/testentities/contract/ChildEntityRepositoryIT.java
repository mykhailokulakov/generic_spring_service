package io.github.mykhailokulakov.testentities.contract;

import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import io.github.mykhailokulakov.testentities.ChildEntity;
import io.github.mykhailokulakov.testentities.ParentEntity;
import io.github.mykhailokulakov.testentities.repository.ParentEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;

class ChildEntityRepositoryIT extends AbstractRepositoryTestContract<ChildEntity> {

  @Autowired private ParentEntityRepository parentRepository;

  @Override
  protected ChildEntity newEntity() {
    var parent = parentRepository.saveAndFlush(RandomEntities.create(ParentEntity.class));
    var child = RandomEntities.create(ChildEntity.class);
    child.setParent(parent);
    return child;
  }

  @Override
  protected void mutate(ChildEntity entity) {
    entity.setValue("mutated");
  }
}
