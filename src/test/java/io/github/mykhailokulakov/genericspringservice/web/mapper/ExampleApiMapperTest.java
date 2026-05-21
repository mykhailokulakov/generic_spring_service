package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateExampleRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.ExampleResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateExampleRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ExampleApiMapperTest {

  private final ExampleApiMapper mapper = Mappers.getMapper(ExampleApiMapper.class);

  private Example sampleModel() {
    return new Example(
        UUID.fromString("22222222-2222-2222-2222-222222222222"),
        "name",
        "description",
        7,
        new BigDecimal("123.45"),
        Instant.parse("2026-05-20T12:00:00Z"),
        ExampleStatus.ACTIVE,
        Set.of("alpha", "beta"),
        Instant.parse("2026-05-19T00:00:00Z"),
        Instant.parse("2026-05-19T01:00:00Z"),
        4L);
  }

  @Test
  void toResponseCopiesEveryField() {
    Example model = sampleModel();

    ExampleResponse response = mapper.toResponse(model);

    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.name()).isEqualTo(model.name());
    assertThat(response.description()).isEqualTo(model.description());
    assertThat(response.quantity()).isEqualTo(model.quantity());
    assertThat(response.price()).isEqualByComparingTo(model.price());
    assertThat(response.occurredAt()).isEqualTo(model.occurredAt());
    assertThat(response.status()).isEqualTo(model.status());
    assertThat(response.tags()).containsExactlyInAnyOrderElementsOf(model.tags());
    assertThat(response.createdAt()).isEqualTo(model.createdAt());
    assertThat(response.updatedAt()).isEqualTo(model.updatedAt());
    assertThat(response.version()).isEqualTo(model.version());
  }

  @Test
  void toResponseReturnsNullForNullModel() {
    assertThat(mapper.toResponse(null)).isNull();
  }

  @Test
  void toModelFromCreateRequestLeavesManagedFieldsNull() {
    CreateExampleRequest request =
        new CreateExampleRequest(
            "name",
            "description",
            10,
            new BigDecimal("19.99"),
            Instant.parse("2026-05-20T12:00:00Z"),
            ExampleStatus.DRAFT,
            Set.of("alpha"));

    Example model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.name()).isEqualTo("name");
    assertThat(model.description()).isEqualTo("description");
    assertThat(model.quantity()).isEqualTo(10);
    assertThat(model.price()).isEqualByComparingTo("19.99");
    assertThat(model.occurredAt()).isEqualTo(Instant.parse("2026-05-20T12:00:00Z"));
    assertThat(model.status()).isEqualTo(ExampleStatus.DRAFT);
    assertThat(model.tags()).containsExactly("alpha");
  }

  @Test
  void toModelFromCreateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((CreateExampleRequest) null)).isNull();
  }

  @Test
  void toModelFromUpdateRequestLeavesManagedFieldsNull() {
    UpdateExampleRequest request =
        new UpdateExampleRequest(
            "name",
            "description",
            10,
            new BigDecimal("19.99"),
            Instant.parse("2026-05-20T12:00:00Z"),
            ExampleStatus.ACTIVE,
            Set.of("alpha", "beta"));

    Example model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.name()).isEqualTo("name");
    assertThat(model.description()).isEqualTo("description");
    assertThat(model.quantity()).isEqualTo(10);
    assertThat(model.price()).isEqualByComparingTo("19.99");
    assertThat(model.occurredAt()).isEqualTo(Instant.parse("2026-05-20T12:00:00Z"));
    assertThat(model.status()).isEqualTo(ExampleStatus.ACTIVE);
    assertThat(model.tags()).containsExactlyInAnyOrder("alpha", "beta");
  }

  @Test
  void toModelFromUpdateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((UpdateExampleRequest) null)).isNull();
  }
}
