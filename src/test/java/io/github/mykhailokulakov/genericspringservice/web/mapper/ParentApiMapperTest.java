package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.domain.model.ParentPatch;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractApiMapperTestContract;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateParentRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.ParentResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchParentRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateParentRequest;
import org.instancio.Instancio;
import org.mapstruct.factory.Mappers;

class ParentApiMapperTest
    extends AbstractApiMapperTestContract<
        Parent,
        ParentResponse,
        CreateParentRequest,
        UpdateParentRequest,
        PatchParentRequest,
        ParentPatch> {

  private final ParentApiMapper mapper = Mappers.getMapper(ParentApiMapper.class);

  @Override
  protected Parent newModel() {
    return Instancio.create(Parent.class);
  }

  @Override
  protected CreateParentRequest newCreateRequest() {
    return Instancio.create(CreateParentRequest.class);
  }

  @Override
  protected UpdateParentRequest newUpdateRequest() {
    return Instancio.create(UpdateParentRequest.class);
  }

  @Override
  protected PatchParentRequest newPatchRequest() {
    return Instancio.create(PatchParentRequest.class);
  }

  @Override
  protected ParentResponse toResponse(Parent model) {
    return mapper.toResponse(model);
  }

  @Override
  protected Parent toModelFromCreate(CreateParentRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected Parent toModelFromUpdate(UpdateParentRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected ParentPatch toModelFromPatch(PatchParentRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected void assertResponseFields(ParentResponse response, Parent model) {
    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.label()).isEqualTo(model.label());
    assertThat(response.createdAt()).isEqualTo(model.createdAt());
    assertThat(response.updatedAt()).isEqualTo(model.updatedAt());
    assertThat(response.version()).isEqualTo(model.version());
  }

  @Override
  protected void assertCreateFields(Parent model, CreateParentRequest request) {
    assertThat(model.label()).isEqualTo(request.label());
  }

  @Override
  protected void assertUpdateFields(Parent model, UpdateParentRequest request) {
    assertThat(model.label()).isEqualTo(request.label());
  }

  @Override
  protected void assertPatchFields(ParentPatch patch, PatchParentRequest request) {
    assertThat(patch.label()).isEqualTo(request.label());
  }
}
