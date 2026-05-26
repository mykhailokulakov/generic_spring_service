package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Owner;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateOwnerRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchOwnerRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateOwnerRequest;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class OwnerApiMapperTest {

  private final OwnerApiMapper mapper = Mappers.getMapper(OwnerApiMapper.class);

  @Test
  void toResponseCopiesEveryField() {
    var model = Instancio.create(Owner.class);

    var response = mapper.toResponse(model);

    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.handle()).isEqualTo(model.handle());
    assertThat(response.exampleId()).isEqualTo(model.exampleId());
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
    var exampleId = UUID.randomUUID();
    var request = new CreateOwnerRequest("owner-handle", exampleId);

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.handle()).isEqualTo("owner-handle");
    assertThat(model.exampleId()).isEqualTo(exampleId);
  }

  @Test
  void toModelFromCreateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((CreateOwnerRequest) null)).isNull();
  }

  @Test
  void toModelFromUpdateRequestLeavesManagedFieldsNull() {
    var request = new UpdateOwnerRequest("updated-handle");

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.exampleId()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.handle()).isEqualTo("updated-handle");
  }

  @Test
  void toModelFromUpdateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((UpdateOwnerRequest) null)).isNull();
  }

  @Test
  void toModelFromPatchRequestMapsFields() {
    var request = new PatchOwnerRequest("patched-handle");

    var patch = mapper.toModel(request);

    assertThat(patch.handle()).isEqualTo("patched-handle");
  }

  @Test
  void toModelFromPatchRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((PatchOwnerRequest) null)).isNull();
  }
}
