package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Left;
import io.github.mykhailokulakov.genericspringservice.domain.model.LeftPatch;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractApiMapperTestContract;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateLeftRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.LeftResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchLeftRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateLeftRequest;
import org.instancio.Instancio;
import org.mapstruct.factory.Mappers;

class LeftApiMapperTest
    extends AbstractApiMapperTestContract<
        Left, LeftResponse, CreateLeftRequest, UpdateLeftRequest, PatchLeftRequest, LeftPatch> {

  private final LeftApiMapper mapper = Mappers.getMapper(LeftApiMapper.class);

  @Override
  protected Left newModel() {
    return Instancio.create(Left.class);
  }

  @Override
  protected CreateLeftRequest newCreateRequest() {
    return Instancio.create(CreateLeftRequest.class);
  }

  @Override
  protected UpdateLeftRequest newUpdateRequest() {
    return Instancio.create(UpdateLeftRequest.class);
  }

  @Override
  protected PatchLeftRequest newPatchRequest() {
    return Instancio.create(PatchLeftRequest.class);
  }

  @Override
  protected LeftResponse toResponse(Left model) {
    return mapper.toResponse(model);
  }

  @Override
  protected Left toModelFromCreate(CreateLeftRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected Left toModelFromUpdate(UpdateLeftRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected LeftPatch toModelFromPatch(PatchLeftRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected void assertResponseFields(LeftResponse response, Left model) {
    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.code()).isEqualTo(model.code());
    assertThat(response.createdAt()).isEqualTo(model.createdAt());
    assertThat(response.updatedAt()).isEqualTo(model.updatedAt());
    assertThat(response.version()).isEqualTo(model.version());
  }

  @Override
  protected void assertCreateFields(Left model, CreateLeftRequest request) {
    assertThat(model.code()).isEqualTo(request.code());
  }

  @Override
  protected void assertUpdateFields(Left model, UpdateLeftRequest request) {
    assertThat(model.code()).isEqualTo(request.code());
  }

  @Override
  protected void assertPatchFields(LeftPatch patch, PatchLeftRequest request) {
    assertThat(patch.code()).isEqualTo(request.code());
  }
}
