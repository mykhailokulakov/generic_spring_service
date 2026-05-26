package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.domain.model.ChildPatch;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractApiMapperTestContract;
import io.github.mykhailokulakov.genericspringservice.web.dto.ChildResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateChildRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchChildRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateChildRequest;
import org.instancio.Instancio;
import org.mapstruct.factory.Mappers;

class ChildApiMapperTest
    extends AbstractApiMapperTestContract<
        Child,
        ChildResponse,
        CreateChildRequest,
        UpdateChildRequest,
        PatchChildRequest,
        ChildPatch> {

  private final ChildApiMapper mapper = Mappers.getMapper(ChildApiMapper.class);

  @Override
  protected Child newModel() {
    return Instancio.create(Child.class);
  }

  @Override
  protected CreateChildRequest newCreateRequest() {
    return Instancio.create(CreateChildRequest.class);
  }

  @Override
  protected UpdateChildRequest newUpdateRequest() {
    return Instancio.create(UpdateChildRequest.class);
  }

  @Override
  protected PatchChildRequest newPatchRequest() {
    return Instancio.create(PatchChildRequest.class);
  }

  @Override
  protected ChildResponse toResponse(Child model) {
    return mapper.toResponse(model);
  }

  @Override
  protected Child toModelFromCreate(CreateChildRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected Child toModelFromUpdate(UpdateChildRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected ChildPatch toModelFromPatch(PatchChildRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected void assertResponseFields(ChildResponse response, Child model) {
    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.value()).isEqualTo(model.value());
    assertThat(response.parentId()).isEqualTo(model.parentId());
    assertThat(response.createdAt()).isEqualTo(model.createdAt());
    assertThat(response.updatedAt()).isEqualTo(model.updatedAt());
    assertThat(response.version()).isEqualTo(model.version());
  }

  @Override
  protected void assertCreateFields(Child model, CreateChildRequest request) {
    assertThat(model.value()).isEqualTo(request.value());
    assertThat(model.parentId()).isEqualTo(request.parentId());
  }

  @Override
  protected void assertUpdateFields(Child model, UpdateChildRequest request) {
    assertThat(model.value()).isEqualTo(request.value());
  }

  @Override
  protected void assertPatchFields(ChildPatch patch, PatchChildRequest request) {
    assertThat(patch.value()).isEqualTo(request.value());
  }
}
