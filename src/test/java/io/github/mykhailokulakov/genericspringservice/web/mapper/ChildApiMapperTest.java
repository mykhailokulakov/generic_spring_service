package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Child;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateChildRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchChildRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateChildRequest;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ChildApiMapperTest {

  private final ChildApiMapper mapper = Mappers.getMapper(ChildApiMapper.class);

  @Test
  void toResponseCopiesEveryField() {
    var model = Instancio.create(Child.class);

    var response = mapper.toResponse(model);

    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.value()).isEqualTo(model.value());
    assertThat(response.parentId()).isEqualTo(model.parentId());
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
    var parentId = UUID.randomUUID();
    var request = new CreateChildRequest("child-value", parentId);

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.value()).isEqualTo("child-value");
    assertThat(model.parentId()).isEqualTo(parentId);
  }

  @Test
  void toModelFromCreateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((CreateChildRequest) null)).isNull();
  }

  @Test
  void toModelFromUpdateRequestLeavesManagedFieldsNull() {
    var request = new UpdateChildRequest("updated-value");

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.parentId()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.value()).isEqualTo("updated-value");
  }

  @Test
  void toModelFromUpdateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((UpdateChildRequest) null)).isNull();
  }

  @Test
  void toModelFromPatchRequestMapsFields() {
    var request = new PatchChildRequest("patched-value");

    var patch = mapper.toModel(request);

    assertThat(patch.value()).isEqualTo("patched-value");
  }

  @Test
  void toModelFromPatchRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((PatchChildRequest) null)).isNull();
  }
}
