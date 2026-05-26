package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
import org.junit.jupiter.api.Test;

public abstract class AbstractApiMapperTestContract<
    Model extends DomainModel,
    Response,
    CreateRequest,
    UpdateRequest,
    PatchRequest,
    Patch> {

  protected abstract Model newModel();

  protected abstract CreateRequest newCreateRequest();

  protected abstract UpdateRequest newUpdateRequest();

  protected abstract PatchRequest newPatchRequest();

  protected abstract Response toResponse(Model model);

  protected abstract Model toModelFromCreate(CreateRequest request);

  protected abstract Model toModelFromUpdate(UpdateRequest request);

  protected abstract Patch toModelFromPatch(PatchRequest request);

  protected abstract void assertResponseFields(Response response, Model model);

  protected abstract void assertCreateFields(Model model, CreateRequest request);

  protected abstract void assertUpdateFields(Model model, UpdateRequest request);

  protected abstract void assertPatchFields(Patch patch, PatchRequest request);

  @Test
  void toResponseCopiesEveryField() {
    var model = newModel();

    var response = toResponse(model);

    assertThat(response).isNotNull();
    assertResponseFields(response, model);
  }

  @Test
  void toResponseReturnsNullForNullModel() {
    assertThat(toResponse(null)).isNull();
  }

  @Test
  void toModelFromCreateRequestLeavesManagedFieldsNull() {
    var request = newCreateRequest();

    var model = toModelFromCreate(request);

    assertThat(model).isNotNull();
    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertCreateFields(model, request);
  }

  @Test
  void toModelFromCreateRequestReturnsNullForNullRequest() {
    assertThat(toModelFromCreate(null)).isNull();
  }

  @Test
  void toModelFromUpdateRequestLeavesManagedFieldsNull() {
    var request = newUpdateRequest();

    var model = toModelFromUpdate(request);

    assertThat(model).isNotNull();
    assertThat(model.id()).isNull();
    assertThat(model.createdAt()).isNull();
    assertThat(model.updatedAt()).isNull();
    assertThat(model.version()).isNull();
    assertUpdateFields(model, request);
  }

  @Test
  void toModelFromUpdateRequestReturnsNullForNullRequest() {
    assertThat(toModelFromUpdate(null)).isNull();
  }

  @Test
  void toModelFromPatchRequestMapsFields() {
    var request = newPatchRequest();

    var patch = toModelFromPatch(request);

    assertThat(patch).isNotNull();
    assertPatchFields(patch, request);
  }

  @Test
  void toModelFromPatchRequestReturnsNullForNullRequest() {
    assertThat(toModelFromPatch(null)).isNull();
  }
}
