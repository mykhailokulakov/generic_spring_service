package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.contract.ManyToOneRepositoryTestContract;

class ChildRepositoryIT extends AbstractRepositoryTestContract<ChildEntity>
    implements ManyToOneRepositoryTestContract<ChildEntity> {}
