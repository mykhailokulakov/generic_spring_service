package io.github.mykhailokulakov.genericspringservice.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.Parent;
import io.github.mykhailokulakov.genericspringservice.web.dto.CreateParentRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.PatchParentRequest;
import io.github.mykhailokulakov.genericspringservice.web.dto.UpdateParentRequest;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ParentApiMapperTest {

  private final ParentApiMapper mapper = Mappers.getMapper(ParentApiMapper.class);

  @Test
  void toResponseCopiesEveryField() {
    var model = Instancio.create(Parent.class);

    var response = mapper.toResponse(model);

    assertThat(response.id()).isEqualTo(model.id());
    assertThat(response.label()).isEqualTo(model.label());
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
    var request = new CreateParentRequest("parent-label");

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.label()).isEqualTo("parent-label");
  }

  @Test
  void toModelFromCreateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((CreateParentRequest) null)).isNull();
  }

  @Test
  void toModelFromUpdateRequestLeavesManagedFieldsNull() {
    var request = new UpdateParentRequest("updated-label");

    var model = mapper.toModel(request);

    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertThat(model.label()).isEqualTo("updated-label");
  }

  @Test
  void toModelFromUpdateRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((UpdateParentRequest) null)).isNull();
  }

  @Test
  void toModelFromPatchRequestMapsFields() {
    var request = new PatchParentRequest("patched-label");

    var patch = mapper.toModel(request);

    assertThat(patch.label()).isEqualTo("patched-label");
  }

  @Test
  void toModelFromPatchRequestReturnsNullForNullRequest() {
    assertThat(mapper.toModel((PatchParentRequest) null)).isNull();
  }
}
