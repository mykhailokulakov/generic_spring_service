package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExamplePatch;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractMapperContractIT;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomModels;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ExampleEntityMapperTest extends AbstractMapperContractIT<ExampleEntity, Example> {

  private final ExampleEntityMapper mapper = Mappers.getMapper(ExampleEntityMapper.class);

  @Override
  protected EntityMapper<ExampleEntity, Example> mapper() {
    return mapper;
  }

  @Override
  protected void assertMappedFields(ExampleEntity entity, Example model) {
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
    assertThat(mapper.toModel(null)).isNull();
  }

  @Test
  void toEntityReturnsNullForNullModel() {
    assertThat(mapper.toEntity(null)).isNull();
  }

  @Test
  void toModelPreservesNullTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).build();
    entity.setTags(null);

    assertThat(mapper.toModel(entity).tags()).isNull();
  }

  @Test
  void toEntityWithNullTagsProducesEmptySet() {
    var model =
        new Example(
            null, "name", null, null, null, null, ExampleStatus.DRAFT, null, null, null, null);

    assertThat(mapper.toEntity(model).getTags()).isEmpty();
  }

  @Test
  void applyReplacementOnNullIsNoOp() {
    var entity = mapper.toEntity(RandomModels.create(Example.class));
    var originalName = entity.getName();

    mapper.applyReplacement(null, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
  }

  @Test
  void applyPatchOnNullIsNoOp() {
    var entity = mapper.toEntity(RandomModels.create(Example.class));
    var originalName = entity.getName();

    mapper.applyPatch(null, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
  }

  @Test
  void applyReplacementSetsTagsOnEntityWithoutTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).build();
    entity.setTags(null);

    mapper.applyReplacement(
        new Example(null, null, null, null, null, null, null, Set.of("only"), null, null, null),
        entity);

    assertThat(entity.getTags()).containsExactly("only");
  }

  @Test
  void applyPatchUpdatesEveryProvidedField() {
    var entity = mapper.toEntity(RandomModels.create(Example.class));
    var patch =
        new ExamplePatch(
            "n",
            "d",
            8,
            new BigDecimal("1.23"),
            Instant.parse("2027-06-01T00:00:00Z"),
            ExampleStatus.ARCHIVED,
            Set.of("only"));

    mapper.applyPatch(patch, entity);

    assertThat(entity.getName()).isEqualTo("n");
    assertThat(entity.getDescription()).isEqualTo("d");
    assertThat(entity.getQuantity()).isEqualTo(8);
    assertThat(entity.getPrice()).isEqualByComparingTo("1.23");
    assertThat(entity.getOccurredAt()).isEqualTo(Instant.parse("2027-06-01T00:00:00Z"));
    assertThat(entity.getStatus()).isEqualTo(ExampleStatus.ARCHIVED);
    assertThat(entity.getTags()).containsExactly("only");
  }

  @Test
  void applyPatchSetsTagsOnEntityWithoutTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).build();
    entity.setTags(null);

    mapper.applyPatch(
        new ExamplePatch(null, null, null, null, null, null, Set.of("x", "y")), entity);

    assertThat(entity.getTags()).containsExactlyInAnyOrder("x", "y");
  }

  @Test
  void applyPatchIgnoresNullFields() {
    var entity = mapper.toEntity(RandomModels.create(Example.class));
    var originalName = entity.getName();
    var originalPrice = entity.getPrice();
    var originalStatus = entity.getStatus();

    mapper.applyPatch(new ExamplePatch(null, "patched", 99, null, null, null, null), entity);

    assertThat(entity.getName()).isEqualTo(originalName);
    assertThat(entity.getDescription()).isEqualTo("patched");
    assertThat(entity.getQuantity()).isEqualTo(99);
    assertThat(entity.getPrice()).isEqualByComparingTo(originalPrice);
    assertThat(entity.getStatus()).isEqualTo(originalStatus);
  }
}
