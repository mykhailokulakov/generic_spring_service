package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractMapperTestContract;
import org.mapstruct.factory.Mappers;

class RightEntityMapperTest extends AbstractMapperTestContract<RightEntity, Right> {

  @Override
  protected RightEntityMapper mapper() {
    return Mappers.getMapper(RightEntityMapper.class);
  }

  @Override
  protected void assertDomainFields(RightEntity entity, Right model) {
    assertThat(entity.getName()).isEqualTo(model.name());
  }
}
