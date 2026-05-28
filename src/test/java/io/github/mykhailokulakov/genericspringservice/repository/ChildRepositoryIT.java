package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.contract.ManyToOneRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import org.springframework.beans.factory.annotation.Autowired;

class ChildRepositoryIT extends AbstractRepositoryTestContract<ChildEntity>
    implements ManyToOneRepositoryTestContract<ChildEntity> {

  @Autowired private ParentRepository parentRepository;

  @Override
  public ChildEntity newEntity() {
    var parent = parentRepository.saveAndFlush(RandomEntities.create(ParentEntity.class));
    var child = RandomEntities.create(ChildEntity.class);
    child.setParent(parent);
    return child;
  }
}
