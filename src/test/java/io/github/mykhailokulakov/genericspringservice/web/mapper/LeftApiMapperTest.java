package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Left;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateLeftRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchLeftRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateLeftRequest;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class LeftApiMapperTest {

  private final LeftApiMapper mapper = Mappers.getMapper(LeftApiMapper.class);

  @Test
  void toResponseCopiesEveryField() {
    var model = Instancio.create(Left.class);

    var response = mapper.toResponse(model);

    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.code()).isEqualTo(model.code());
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
    var request = new CreateLeftRequest("LEFT-001");

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.code()).isEqualTo("LEFT-001");
  }

  @Test
  void toModelFromCreateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((CreateLeftRequest) null)).isNull();
  }

  @Test
  void toModelFromUpdateRequestLeavesManagedFieldsNull() {
    var request = new UpdateLeftRequest("LEFT-002");

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.code()).isEqualTo("LEFT-002");
  }

  @Test
  void toModelFromUpdateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((UpdateLeftRequest) null)).isNull();
  }

  @Test
  void toModelFromPatchRequestMapsFields() {
    var request = new PatchLeftRequest("LEFT-PATCHED");

    var patch = mapper.toModel(request);

    assertThat(patch.code()).isEqualTo("LEFT-PATCHED");
  }

  @Test
  void toModelFromPatchRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((PatchLeftRequest) null)).isNull();
  }
}
