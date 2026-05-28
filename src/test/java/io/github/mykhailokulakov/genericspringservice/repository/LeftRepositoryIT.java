package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.contract.ManyToManyRepositoryTestContract;

class LeftRepositoryIT extends AbstractRepositoryTestContract<LeftEntity>
    implements ManyToManyRepositoryTestContract<LeftEntity> {}
