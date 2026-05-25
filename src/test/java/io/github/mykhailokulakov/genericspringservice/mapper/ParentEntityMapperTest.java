package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentPatch;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractPatchableMapperTestContract;
import org.mapstruct.factory.Mappers;

class ParentEntityMapperTest
    extends AbstractPatchableMapperTestContract<ParentEntity, Parent, ParentPatch> {

  @Override
  protected ParentEntityMapper mapper() {
    return Mappers.getMapper(ParentEntityMapper.class);
  }

  @Override
  protected void assertDomainFields(ParentEntity entity, Parent model) {
    assertThat(entity.getLabel()).isEqualTo(model.label());
  }
}
