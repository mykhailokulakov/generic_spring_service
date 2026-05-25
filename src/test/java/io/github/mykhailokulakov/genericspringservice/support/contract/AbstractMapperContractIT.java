package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.ModelFixture;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RepoFixture;
import java.util.List;
import java.util.function.Function;
import org.instancio.Instancio;
import org.instancio.settings.AssignmentType;
import org.instancio.settings.Keys;
import org.instancio.settings.OnSetFieldError;
import org.instancio.settings.Settings;
import org.junit.jupiter.api.Test;

public abstract class AbstractMapperContractIT<E extends SoftDeletable, M> {

  private static final Settings ALL_FIELDS_SETTINGS =
      Settings.create()
          .set(Keys.ASSIGNMENT_TYPE, AssignmentType.FIELD)
          .set(Keys.ON_SET_FIELD_ERROR, OnSetFieldError.IGNORE)
          .lock();

  protected abstract RepoFixture<E> repoFixture();

  protected abstract ModelFixture<M> modelFixture();

  protected abstract Function<E, M> toModel();

  protected abstract Function<M, E> toEntity();

  protected abstract Function<List<E>, List<M>> toModelList();

  protected abstract Function<List<M>, List<E>> toEntityList();

  protected abstract void applyPatch(M source, E target);

  protected abstract void assertMappedFields(E entity, M model);

  private E fullyPopulatedEntity() {
    return Instancio.of(repoFixture().entityType()).withSettings(ALL_FIELDS_SETTINGS).create();
  }

  @Test
  void toModel_copiesAuditFieldsFromEntity() {
    var entity = fullyPopulatedEntity();

    var model = toModel().apply(entity);

    assertThat(model).isNotNull();
    assertMappedFields(toEntity().apply(model), model);
  }

  @Test
  void toModelList_mapsEveryElement() {
    var entities =
        List.of(
            repoFixture().newPersistable(),
            repoFixture().newPersistable(),
            repoFixture().newPersistable());

    var models = toModelList().apply(entities);

    assertThat(models).hasSameSizeAs(entities);
    for (int i = 0; i < entities.size(); i++) {
      assertMappedFields(entities.get(i), models.get(i));
    }
  }

  @Test
  void toEntityList_mapsEveryElement() {
    var models =
        List.of(modelFixture().newModel(), modelFixture().newModel(), modelFixture().newModel());

    var entities = toEntityList().apply(models);

    assertThat(entities).hasSameSizeAs(models);
    for (int i = 0; i < models.size(); i++) {
      assertMappedFields(entities.get(i), models.get(i));
    }
  }

  @Test
  void applyPatch_updatesMappedFieldsOnly() {
    var entity = fullyPopulatedEntity();
    var idBefore = entity.getId();
    var createdAtBefore = entity.getCreatedAt();
    var updatedAtBefore = entity.getUpdatedAt();
    var versionBefore = entity.getVersion();

    var source = modelFixture().newModel();
    applyPatch(source, entity);

    assertMappedFields(entity, source);
    assertThat(entity.getId()).isEqualTo(idBefore);
    assertThat(entity.getCreatedAt()).isEqualTo(createdAtBefore);
    assertThat(entity.getUpdatedAt()).isEqualTo(updatedAtBefore);
    assertThat(entity.getVersion()).isEqualTo(versionBefore);
  }

  @Test
  void roundTrip_preservesMappedFields() {
    var original = repoFixture().newPersistable();

    var model = toModel().apply(original);
    assertMappedFields(original, model);

    var backToEntity = toEntity().apply(model);
    assertMappedFields(backToEntity, model);
  }
}
