package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractPatchableMapperTestContract;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class OwnerEntityMapperTest extends AbstractPatchableMapperTestContract<OwnerEntity, Owner> {

  @Override
  protected OwnerEntityMapper mapper() {
    return Mappers.getMapper(OwnerEntityMapper.class);
  }

  @Override
  protected void assertDomainFields(OwnerEntity entity, Owner model) {
    assertThat(entity.getHandle()).isEqualTo(model.handle());
  }

  @Test
  void toModel_mapsExampleIdFromAssociation() {
    var exampleId = UUID.randomUUID();
    var example =
        ExampleEntity.builder().id(exampleId).name("e").status(ExampleStatus.ACTIVE).build();
    var entity = newEntity();
    entity.setExample(example);

    var model = mapper().toModel(entity);

    assertThat(model.exampleId()).isEqualTo(exampleId);
  }

  @Test
  void toModel_returnsNullExampleIdWhenExampleIsNull() {
    var entity = newEntity();
    entity.setExample(null);

    var model = mapper().toModel(entity);

    assertThat(model.exampleId()).isNull();
  }
}
