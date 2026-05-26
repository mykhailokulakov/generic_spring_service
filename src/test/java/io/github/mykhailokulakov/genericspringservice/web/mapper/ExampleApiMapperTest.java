package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Example;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExamplePatch;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractApiMapperTestContract;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateExampleRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.ExampleResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchExampleRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateExampleRequest;
import org.instancio.Instancio;
import org.mapstruct.factory.Mappers;

class ExampleApiMapperTest
    extends AbstractApiMapperTestContract<
        Example,
        ExampleResponse,
        CreateExampleRequest,
        UpdateExampleRequest,
        PatchExampleRequest,
        ExamplePatch> {

  private final ExampleApiMapper mapper = Mappers.getMapper(ExampleApiMapper.class);

  @Override
  protected Example newModel() {
    return Instancio.create(Example.class);
  }

  @Override
  protected CreateExampleRequest newCreateRequest() {
    return Instancio.create(CreateExampleRequest.class);
  }

  @Override
  protected UpdateExampleRequest newUpdateRequest() {
    return Instancio.create(UpdateExampleRequest.class);
  }

  @Override
  protected PatchExampleRequest newPatchRequest() {
    return Instancio.create(PatchExampleRequest.class);
  }

  @Override
  protected ExampleResponse toResponse(Example model) {
    return mapper.toResponse(model);
  }

  @Override
  protected Example toModelFromCreate(CreateExampleRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected Example toModelFromUpdate(UpdateExampleRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected ExamplePatch toModelFromPatch(PatchExampleRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected void assertResponseFields(ExampleResponse response, Example model) {
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

  @Override
  protected void assertCreateFields(Example model, CreateExampleRequest request) {
    assertThat(model.name()).isEqualTo(request.name());
    assertThat(model.description()).isEqualTo(request.description());
    assertThat(model.quantity()).isEqualTo(request.quantity());
    assertThat(model.price()).isEqualByComparingTo(request.price());
    assertThat(model.occurredAt()).isEqualTo(request.occurredAt());
    assertThat(model.status()).isEqualTo(request.status());
    assertThat(model.tags()).containsExactlyInAnyOrderElementsOf(request.tags());
  }

  @Override
  protected void assertUpdateFields(Example model, UpdateExampleRequest request) {
    assertThat(model.name()).isEqualTo(request.name());
    assertThat(model.description()).isEqualTo(request.description());
    assertThat(model.quantity()).isEqualTo(request.quantity());
    assertThat(model.price()).isEqualByComparingTo(request.price());
    assertThat(model.occurredAt()).isEqualTo(request.occurredAt());
    assertThat(model.status()).isEqualTo(request.status());
    assertThat(model.tags()).containsExactlyInAnyOrderElementsOf(request.tags());
  }

  @Override
  protected void assertPatchFields(ExamplePatch patch, PatchExampleRequest request) {
    assertThat(patch.name()).isEqualTo(request.name());
    assertThat(patch.description()).isEqualTo(request.description());
    assertThat(patch.quantity()).isEqualTo(request.quantity());
    assertThat(patch.price()).isEqualByComparingTo(request.price());
    assertThat(patch.occurredAt()).isEqualTo(request.occurredAt());
    assertThat(patch.status()).isEqualTo(request.status());
    assertThat(patch.tags()).containsExactlyInAnyOrderElementsOf(request.tags());
  }
}
