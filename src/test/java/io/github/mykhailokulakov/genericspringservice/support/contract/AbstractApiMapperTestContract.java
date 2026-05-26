package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
import org.junit.jupiter.api.Test;

public abstract class AbstractApiMapperTestContract<M extends DomainModel, R, C, U, PR, P> {

  protected abstract M newModel();

  protected abstract C newCreateRequest();

  protected abstract U newUpdateRequest();

  protected abstract PR newPatchRequest();

  protected abstract R toResponse(M model);

  protected abstract M toModelFromCreate(C request);

  protected abstract M toModelFromUpdate(U request);

  protected abstract P toModelFromPatch(PR request);

  protected abstract void assertResponseFields(R response, M model);

  protected abstract void assertCreateFields(M model, C request);

  protected abstract void assertUpdateFields(M model, U request);

  protected abstract void assertPatchFields(P patch, PR request);

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
