package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.contract.OneToOneRepositoryTestContract;

class OwnerRepositoryIT extends AbstractRepositoryTestContract<OwnerEntity>
    implements OneToOneRepositoryTestContract<OwnerEntity> {}
