package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractMapperTestContract;
import org.mapstruct.factory.Mappers;

class ParentEntityMapperTest extends AbstractMapperTestContract<ParentEntity, Parent> {

  @Override
  protected ParentEntityMapper mapper() {
    return Mappers.getMapper(ParentEntityMapper.class);
  }

  @Override
  protected void assertDomainFields(ParentEntity entity, Parent model) {
    assertThat(entity.getLabel()).isEqualTo(model.label());
  }
}
