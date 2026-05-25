package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExamplePatch;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractMapperContractIT;
import java.util.Set;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ExampleEntityMapperTest extends AbstractMapperContractIT<ExampleEntity, Example> {

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
  void toModelReturnsNullForNullEntity() {
    assertThat(mapper().toModel(null)).isNull();
  }

  @Test
  void toEntityReturnsNullForNullModel() {
    assertThat(mapper().toEntity(null)).isNull();
  }

  @Test
  void toModelPreservesNullTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).build();
    entity.setTags(null);

    assertThat(mapper().toModel(entity).tags()).isNull();
  }

  @Test
  void toEntityWithNullTagsProducesEmptySet() {
    var model = Instancio.create(Example.class).toBuilder().tags(null).build();

    assertThat(mapper().toEntity(model).getTags()).isEmpty();
  }

  @Test
  void applyReplacementOnNullIsNoOp() {
    var entity = mapper().toEntity(newModel());
    var originalName = entity.getName();

    mapper().applyReplacement(null, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
  }

  @Test
  void applyPatchOnNullIsNoOp() {
    var entity = mapper().toEntity(newModel());
    var originalName = entity.getName();

    mapper().applyPatch(null, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
  }

  @Test
  void applyReplacementSetsTagsOnEntityWithoutTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).build();
    entity.setTags(null);

    var replacement = Instancio.create(Example.class).toBuilder().tags(Set.of("only")).build();
    mapper().applyReplacement(replacement, entity);

    assertThat(entity.getTags()).containsExactly("only");
  }

  @Test
  void applyPatchUpdatesEveryProvidedField() {
    var entity = mapper().toEntity(newModel());
    var patch = Instancio.create(ExamplePatch.class);

    mapper().applyPatch(patch, entity);

    assertThat(entity.getName()).isEqualTo(patch.name());
    assertThat(entity.getDescription()).isEqualTo(patch.description());
    assertThat(entity.getQuantity()).isEqualTo(patch.quantity());
    assertThat(entity.getPrice()).isEqualByComparingTo(patch.price());
    assertThat(entity.getOccurredAt()).isEqualTo(patch.occurredAt());
    assertThat(entity.getStatus()).isEqualTo(patch.status());
    assertThat(entity.getTags()).containsExactlyInAnyOrderElementsOf(patch.tags());
  }

  @Test
  void applyPatchSetsTagsOnEntityWithoutTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).build();
    entity.setTags(null);

    var patch = Instancio.create(ExamplePatch.class).toBuilder().tags(Set.of("x", "y")).build();
    mapper().applyPatch(patch, entity);

    assertThat(entity.getTags()).containsExactlyInAnyOrder("x", "y");
  }

  @Test
  void applyPatchIgnoresNullFields() {
    var entity = mapper().toEntity(newModel());
    var originalName = entity.getName();
    var originalPrice = entity.getPrice();
    var originalStatus = entity.getStatus();

    var patch = ExamplePatch.builder().description("patched").quantity(99).build();
    mapper().applyPatch(patch, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
    assertThat(entity.getDescription()).isEqualTo("patched");
    assertThat(entity.getQuantity()).isEqualTo(99);
    assertThat(entity.getPrice()).isEqualByComparingTo(originalPrice);
    assertThat(entity.getStatus()).isEqualTo(originalStatus);
  }
}
