package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractPatchableMapperTestContract;
import java.util.Set;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ExampleEntityMapperTest extends AbstractPatchableMapperTestContract<ExampleEntity, Example> {

  @Override
  protected ExampleEntityMapper mapper() {
    return Mappers.getMapper(ExampleEntityMapper.class);
  }

  @Override
  protected void assertDomainFields(ExampleEntity entity, Example model) {
    assertThat(entity.getName()).isEqualTo(model.name());
    assertThat(entity.getDescription()).isEqualTo(model.description());
    assertThat(entity.getQuantity()).isEqualTo(model.quantity());
    if (model.price() != null) {
      assertThat(entity.getPrice()).isEqualByComparingTo(model.price());
    } else {
      assertThat(entity.getPrice()).isNull();
    }
    assertThat(entity.getOccurredAt()).isEqualTo(model.occurredAt());
    assertThat(entity.getStatus()).isEqualTo(model.status());
    if (model.tags() != null) {
      assertThat(entity.getTags()).containsExactlyInAnyOrderElementsOf(model.tags());
    }
  }

  @Test
  void toModelPreservesNullTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).tags(null).build();

    assertThat(mapper().toModel(entity).tags()).isNull();
  }

  @Test
  void toEntityWithNullTagsProducesEmptySet() {
    var model = Instancio.create(Example.class).toBuilder().tags(null).build();

    assertThat(mapper().toEntity(model).getTags()).isEmpty();
  }

  @Test
  void applyReplacementSetsTagsOnEntityWithoutTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).tags(null).build();

    var replacement = Instancio.create(Example.class).toBuilder().tags(Set.of("only")).build();
    mapper().applyReplacement(replacement, entity);

    assertThat(entity.getTags()).containsExactly("only");
  }

  @Test
  void applyPatchSetsTagsOnEntityWithoutTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).tags(null).build();

    var patch = Instancio.create(Example.class).toBuilder().tags(Set.of("x", "y")).build();
    mapper().applyPatch(patch, entity);

    assertThat(entity.getTags()).containsExactlyInAnyOrder("x", "y");
  }
}
