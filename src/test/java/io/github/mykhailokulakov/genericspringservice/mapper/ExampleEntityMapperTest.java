package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExamplePatch;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractMapperContractIT;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.ExampleFixtures;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.ModelFixture;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RepoFixture;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ExampleEntityMapperTest extends AbstractMapperContractIT<ExampleEntity, Example> {

  private final ExampleEntityMapper mapper = Mappers.getMapper(ExampleEntityMapper.class);

  @Override
  protected RepoFixture<ExampleEntity> repoFixture() {
    return ExampleFixtures.INSTANCE;
  }

  @Override
  protected ModelFixture<Example> modelFixture() {
    return ExampleFixtures.INSTANCE;
  }

  @Override
  protected Function<ExampleEntity, Example> toModel() {
    return mapper::toModel;
  }

  @Override
  protected Function<Example, ExampleEntity> toEntity() {
    return mapper::toEntity;
  }

  @Override
  protected Function<List<ExampleEntity>, List<Example>> toModelList() {
    return entities -> entities.stream().map(mapper::toModel).toList();
  }

  @Override
  protected Function<List<Example>, List<ExampleEntity>> toEntityList() {
    return models -> models.stream().map(mapper::toEntity).toList();
  }

  @Override
  protected void applyPatch(Example source, ExampleEntity target) {
    mapper.applyReplacement(source, target);
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

    var model = mapper.toModel(entity);

    assertThat(model.tags()).isNull();
  }

  @Test
  void toEntityWithNullTagsProducesNoTags() {
    var model =
        new Example(
            null, "name", null, null, null, null, ExampleStatus.DRAFT, null, null, null, null);

    var entity = mapper.toEntity(model);

    assertThat(entity.getTags()).isEmpty();
  }

  @Test
  void applyReplacementOnNullReplacementIsNoOp() {
    var entity =
        mapper.toEntity(
            new Example(
                null,
                "name",
                "desc",
                7,
                new BigDecimal("1.00"),
                Instant.parse("2026-05-20T12:00:00Z"),
                ExampleStatus.ACTIVE,
                new HashSet<>(Set.of("alpha")),
                null,
                null,
                null));
    var originalName = entity.getName();

    mapper.applyReplacement(null, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
  }

  @Test
  void applyPatchOnNullPatchIsNoOp() {
    var entity =
        mapper.toEntity(
            new Example(
                null,
                "name",
                "desc",
                7,
                new BigDecimal("1.00"),
                Instant.parse("2026-05-20T12:00:00Z"),
                ExampleStatus.ACTIVE,
                new HashSet<>(Set.of("alpha")),
                null,
                null,
                null));
    var originalName = entity.getName();

    mapper.applyPatch(null, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
  }

  @Test
  void applyReplacementSetsTagsOnEntityWithoutTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).build();
    entity.setTags(null);
    var replacement =
        new Example(null, null, null, null, null, null, null, Set.of("only"), null, null, null);

    mapper.applyReplacement(replacement, entity);

    assertThat(entity.getTags()).containsExactly("only");
  }

  @Test
  void applyPatchUpdatesEveryProvidedField() {
    var entity =
        mapper.toEntity(
            new Example(
                null,
                "name",
                "desc",
                7,
                new BigDecimal("123.45"),
                Instant.parse("2026-05-20T12:00:00Z"),
                ExampleStatus.ACTIVE,
                new HashSet<>(Set.of("alpha", "beta")),
                null,
                null,
                null));
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
    var patch = new ExamplePatch(null, null, null, null, null, null, Set.of("x", "y"));

    mapper.applyPatch(patch, entity);

    assertThat(entity.getTags()).containsExactlyInAnyOrder("x", "y");
  }

  @Test
  void applyPatchIgnoresNullFields() {
    var entity =
        mapper.toEntity(
            new Example(
                null,
                "name",
                "desc",
                7,
                new BigDecimal("123.45"),
                Instant.parse("2026-05-20T12:00:00Z"),
                ExampleStatus.ACTIVE,
                new HashSet<>(Set.of("alpha", "beta")),
                null,
                null,
                null));
    var originalName = entity.getName();
    BigDecimal originalPrice = entity.getPrice();
    var originalTags = new HashSet<>(entity.getTags());

    var patch = new ExamplePatch(null, "patched-description", 99, null, null, null, null);

    mapper.applyPatch(patch, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
    assertThat(entity.getDescription()).isEqualTo("patched-description");
    assertThat(entity.getQuantity()).isEqualTo(99);
    assertThat(entity.getPrice()).isEqualByComparingTo(originalPrice);
    assertThat(entity.getStatus()).isEqualTo(ExampleStatus.ACTIVE);
    assertThat(entity.getTags()).containsExactlyInAnyOrderElementsOf(originalTags);
  }
}
