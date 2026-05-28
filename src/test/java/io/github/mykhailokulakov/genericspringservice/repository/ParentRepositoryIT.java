package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.contract.OneToManyRepositoryTestContract;

class ParentRepositoryIT extends AbstractRepositoryTestContract<ParentEntity>
    implements OneToManyRepositoryTestContract<ParentEntity> {}
