package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.domain.model.RightPatch;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractApiMapperTestContract;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateRightRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchRightRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.RightResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateRightRequest;
import org.instancio.Instancio;
import org.mapstruct.factory.Mappers;

class RightApiMapperTest
    extends AbstractApiMapperTestContract<
        Right,
        RightResponse,
        CreateRightRequest,
        UpdateRightRequest,
        PatchRightRequest,
        RightPatch> {

  private final RightApiMapper mapper = Mappers.getMapper(RightApiMapper.class);

  @Override
  protected Right newModel() {
    return Instancio.create(Right.class);
  }

  @Override
  protected CreateRightRequest newCreateRequest() {
    return Instancio.create(CreateRightRequest.class);
  }

  @Override
  protected UpdateRightRequest newUpdateRequest() {
    return Instancio.create(UpdateRightRequest.class);
  }

  @Override
  protected PatchRightRequest newPatchRequest() {
    return Instancio.create(PatchRightRequest.class);
  }

  @Override
  protected RightResponse toResponse(Right model) {
    return mapper.toResponse(model);
  }

  @Override
  protected Right toModelFromCreate(CreateRightRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected Right toModelFromUpdate(UpdateRightRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected RightPatch toModelFromPatch(PatchRightRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected void assertResponseFields(RightResponse response, Right model) {
    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.name()).isEqualTo(model.name());
    assertThat(response.createdAt()).isEqualTo(model.createdAt());
    assertThat(response.updatedAt()).isEqualTo(model.updatedAt());
    assertThat(response.version()).isEqualTo(model.version());
  }

  @Override
  protected void assertCreateFields(Right model, CreateRightRequest request) {
    assertThat(model.name()).isEqualTo(request.name());
  }

  @Override
  protected void assertUpdateFields(Right model, UpdateRightRequest request) {
    assertThat(model.name()).isEqualTo(request.name());
  }

  @Override
  protected void assertPatchFields(RightPatch patch, PatchRightRequest request) {
    assertThat(patch.name()).isEqualTo(request.name());
  }
}
