package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractPatchableMapperTestContract;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ChildEntityMapperTest extends AbstractPatchableMapperTestContract<ChildEntity, Child> {

  @Override
  protected ChildEntityMapper mapper() {
    return Mappers.getMapper(ChildEntityMapper.class);
  }

  @Override
  protected void assertDomainFields(ChildEntity entity, Child model) {
    assertThat(entity.getValue()).isEqualTo(model.value());
  }

  @Test
  void toModel_mapsParentIdFromAssociation() {
    var parentId = UUID.randomUUID();
    var parent = ParentEntity.builder().id(parentId).label("p").build();
    var entity = newEntity();
    entity.setParent(parent);

    var model = mapper().toModel(entity);

    assertThat(model.parentId()).isEqualTo(parentId);
  }

  @Test
  void toModel_returnsNullParentIdWhenParentIsNull() {
    var entity = newEntity();
    entity.setParent(null);

    var model = mapper().toModel(entity);

    assertThat(model.parentId()).isNull();
  }
}
