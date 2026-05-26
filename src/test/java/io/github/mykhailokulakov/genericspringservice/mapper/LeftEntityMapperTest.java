package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Left;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractPatchableMapperTestContract;
import org.mapstruct.factory.Mappers;

class LeftEntityMapperTest extends AbstractPatchableMapperTestContract<LeftEntity, Left> {

  @Override
  protected LeftEntityMapper mapper() {
    return Mappers.getMapper(LeftEntityMapper.class);
  }

  @Override
  protected void assertDomainFields(LeftEntity entity, Left model) {
    assertThat(entity.getCode()).isEqualTo(model.code());
  }
}
