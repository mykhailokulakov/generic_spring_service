package io.github.mykhailokulakov.genericspringservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExamplePatch;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ExampleEntityMapperTest {

  private final ExampleEntityMapper mapper = Mappers.getMapper(ExampleEntityMapper.class);

  private Example sampleModel() {
    return new Example(
        UUID.fromString("11111111-1111-1111-1111-111111111111"),
        "name",
        "description",
        7,
        new BigDecimal("123.45"),
        Instant.parse("2026-05-20T12:00:00Z"),
        ExampleStatus.ACTIVE,
        new HashSet<>(Set.of("alpha", "beta", "gamma")),
        Instant.parse("2026-05-19T00:00:00Z"),
        Instant.parse("2026-05-19T01:00:00Z"),
        4L);
  }

  @Test
  void roundTripPreservesAllFieldsIncludingTags() {
    Example original = sampleModel();

    ExampleEntity entity = mapper.toEntity(original);
    Example back = mapper.toModel(entity);

    assertThat(back.id()).isEqualTo(original.id());
    assertThat(back.name()).isEqualTo(original.name());
    assertThat(back.description()).isEqualTo(original.description());
    assertThat(back.quantity()).isEqualTo(original.quantity());
    assertThat(back.price()).isEqualByComparingTo(original.price());
    assertThat(back.occurredAt()).isEqualTo(original.occurredAt());
    assertThat(back.status()).isEqualTo(original.status());
    assertThat(back.tags()).containsExactlyInAnyOrderElementsOf(original.tags());
    assertThat(back.createdAt()).isEqualTo(original.createdAt());
    assertThat(back.updatedAt()).isEqualTo(original.updatedAt());
    assertThat(back.version()).isEqualTo(original.version());
  }

  @Test
  void applyReplacementOverwritesAllFields() {
    ExampleEntity entity = mapper.toEntity(sampleModel());
    Example replacement =
        new Example(
            null,
            "new-name",
            "new-description",
            42,
            new BigDecimal("9.99"),
            Instant.parse("2027-01-01T00:00:00Z"),
            ExampleStatus.ARCHIVED,
            new HashSet<>(Set.of("zeta")),
            null,
            null,
            null);

    mapper.applyReplacement(replacement, entity);

    assertThat(entity.getName()).isEqualTo("new-name");
    assertThat(entity.getDescription()).isEqualTo("new-description");
    assertThat(entity.getQuantity()).isEqualTo(42);
    assertThat(entity.getPrice()).isEqualByComparingTo("9.99");
    assertThat(entity.getOccurredAt()).isEqualTo(Instant.parse("2027-01-01T00:00:00Z"));
    assertThat(entity.getStatus()).isEqualTo(ExampleStatus.ARCHIVED);
    assertThat(entity.getTags()).containsExactly("zeta");
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

    Example model = mapper.toModel(entity);

    assertThat(model.tags()).isNull();
  }

  @Test
  void toEntityWithNullTagsProducesNoTags() {
    Example model =
        new Example(
            null, "name", null, null, null, null, ExampleStatus.DRAFT, null, null, null, null);

    ExampleEntity entity = mapper.toEntity(model);

    assertThat(entity.getTags()).isEmpty();
  }

  @Test
  void applyReplacementOnNullReplacementIsNoOp() {
    ExampleEntity entity = mapper.toEntity(sampleModel());
    String originalName = entity.getName();

    mapper.applyReplacement(null, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
  }

  @Test
  void applyPatchOnNullPatchIsNoOp() {
    ExampleEntity entity = mapper.toEntity(sampleModel());
    String originalName = entity.getName();

    mapper.applyPatch(null, entity);

    assertThat(entity.getName()).isEqualTo(originalName);
  }

  @Test
  void applyReplacementSetsTagsOnEntityWithoutTags() {
    var entity = ExampleEntity.builder().name("e").status(ExampleStatus.DRAFT).build();
    entity.setTags(null);
    Example replacement =
        new Example(null, null, null, null, null, null, null, Set.of("only"), null, null, null);

    mapper.applyReplacement(replacement, entity);

    assertThat(entity.getTags()).containsExactly("only");
  }

  @Test
  void applyReplacementMapsNullsButPreservesManagedFields() {
    ExampleEntity entity = mapper.toEntity(sampleModel());
    UUID id = entity.getId();
    Instant createdAt = entity.getCreatedAt();
    Instant updatedAt = entity.getUpdatedAt();
    Long version = entity.getVersion();
    var empty = new Example(null, null, null, null, null, null, null, null, null, null, null);

    mapper.applyReplacement(empty, entity);

    assertThat(entity.getName()).isNull();
    assertThat(entity.getDescription()).isNull();
    assertThat(entity.getQuantity()).isNull();
    assertThat(entity.getPrice()).isNull();
    assertThat(entity.getOccurredAt()).isNull();
    assertThat(entity.getStatus()).isNull();
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
    assertThat(entity.getVersion()).isEqualTo(version);
  }

  @Test
  void applyPatchUpdatesEveryProvidedField() {
    ExampleEntity entity = mapper.toEntity(sampleModel());
    ExamplePatch patch =
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
    ExampleEntity entity = mapper.toEntity(sampleModel());
    String originalName = entity.getName();
    BigDecimal originalPrice = entity.getPrice();
    Set<String> originalTags = new HashSet<>(entity.getTags());

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
