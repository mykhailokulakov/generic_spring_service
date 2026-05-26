package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.domain.model.OwnerPatch;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractApiMapperTestContract;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateOwnerRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.OwnerResponse;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchOwnerRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateOwnerRequest;
import org.instancio.Instancio;
import org.mapstruct.factory.Mappers;

class OwnerApiMapperTest
    extends AbstractApiMapperTestContract<
        Owner,
        OwnerResponse,
        CreateOwnerRequest,
        UpdateOwnerRequest,
        PatchOwnerRequest,
        OwnerPatch> {

  private final OwnerApiMapper mapper = Mappers.getMapper(OwnerApiMapper.class);

  @Override
  protected Owner newModel() {
    return Instancio.create(Owner.class);
  }

  @Override
  protected CreateOwnerRequest newCreateRequest() {
    return Instancio.create(CreateOwnerRequest.class);
  }

  @Override
  protected UpdateOwnerRequest newUpdateRequest() {
    return Instancio.create(UpdateOwnerRequest.class);
  }

  @Override
  protected PatchOwnerRequest newPatchRequest() {
    return Instancio.create(PatchOwnerRequest.class);
  }

  @Override
  protected OwnerResponse toResponse(Owner model) {
    return mapper.toResponse(model);
  }

  @Override
  protected Owner toModelFromCreate(CreateOwnerRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected Owner toModelFromUpdate(UpdateOwnerRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected OwnerPatch toModelFromPatch(PatchOwnerRequest request) {
    return mapper.toModel(request);
  }

  @Override
  protected void assertResponseFields(OwnerResponse response, Owner model) {
    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.handle()).isEqualTo(model.handle());
    assertThat(response.exampleId()).isEqualTo(model.exampleId());
    assertThat(response.createdAt()).isEqualTo(model.createdAt());
    assertThat(response.updatedAt()).isEqualTo(model.updatedAt());
    assertThat(response.version()).isEqualTo(model.version());
  }

  @Override
  protected void assertCreateFields(Owner model, CreateOwnerRequest request) {
    assertThat(model.handle()).isEqualTo(request.handle());
    assertThat(model.exampleId()).isEqualTo(request.exampleId());
  }

  @Override
  protected void assertUpdateFields(Owner model, UpdateOwnerRequest request) {
    assertThat(model.handle()).isEqualTo(request.handle());
  }

  @Override
  protected void assertPatchFields(OwnerPatch patch, PatchOwnerRequest request) {
    assertThat(patch.handle()).isEqualTo(request.handle());
  }
}
