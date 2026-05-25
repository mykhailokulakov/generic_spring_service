package io.github.mykhailokulakov.testentities.contract;

import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.testentities.ProfileEntity;

class ProfileEntityRepositoryIT extends AbstractRepositoryTestContract<ProfileEntity> {

  @Override
  protected void mutate(ProfileEntity entity) {
    entity.setBio("mutated");
  }
}
