package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Right;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateRightRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchRightRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateRightRequest;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class RightApiMapperTest {

  private final RightApiMapper mapper = Mappers.getMapper(RightApiMapper.class);

  @Test
  void toResponseCopiesEveryField() {
    var model = Instancio.create(Right.class);

    var response = mapper.toResponse(model);

    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.name()).isEqualTo(model.name());
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
    var request = new CreateRightRequest("READ_USERS");

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.name()).isEqualTo("READ_USERS");
  }

  @Test
  void toModelFromCreateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((CreateRightRequest) null)).isNull();
  }

  @Test
  void toModelFromUpdateRequestLeavesManagedFieldsNull() {
    var request = new UpdateRightRequest("WRITE_USERS");

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.name()).isEqualTo("WRITE_USERS");
  }

  @Test
  void toModelFromUpdateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((UpdateRightRequest) null)).isNull();
  }

  @Test
  void toModelFromPatchRequestMapsFields() {
    var request = new PatchRightRequest("PATCHED_NAME");

    var patch = mapper.toModel(request);

    assertThat(patch.name()).isEqualTo("PATCHED_NAME");
  }

  @Test
  void toModelFromPatchRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((PatchRightRequest) null)).isNull();
  }
}
