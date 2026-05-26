package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import org.instancio.Instancio;
import org.springframework.beans.factory.annotation.Autowired;

class ChildRepositoryIT extends AbstractRepositoryTestContract<ChildEntity> {

  @Autowired private ParentRepository parentRepository;

  @Override
  protected ChildEntity newEntity() {
    var parent = parentRepository.saveAndFlush(RandomEntities.create(ParentEntity.class));
    var child = RandomEntities.create(ChildEntity.class);
    child.setParent(parent);
    return child;
  }

  @Override
  protected void mutate(ChildEntity entity) {
    entity.setValue(Instancio.create(String.class));
  }
}
