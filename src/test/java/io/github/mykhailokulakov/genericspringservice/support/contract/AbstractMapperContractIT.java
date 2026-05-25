package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.mapper.EntityMapper;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomModels;
import java.lang.reflect.ParameterizedType;
import java.util.List;
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

  private Class<E> entityType;
  private Class<M> modelType;

  protected abstract EntityMapper<E, M> mapper();

  protected abstract void assertMappedFields(E entity, M model);

  @SuppressWarnings("unchecked")
  private Class<E> entityType() {
    if (entityType == null) {
      var superclass = (ParameterizedType) getClass().getGenericSuperclass();
      entityType = (Class<E>) superclass.getActualTypeArguments()[0];
    }
    return entityType;
  }

  @SuppressWarnings("unchecked")
  private Class<M> modelType() {
    if (modelType == null) {
      var superclass = (ParameterizedType) getClass().getGenericSuperclass();
      modelType = (Class<M>) superclass.getActualTypeArguments()[1];
    }
    return modelType;
  }

  private E newEntity() {
    return RandomEntities.create(entityType());
  }

  private M newModel() {
    return RandomModels.create(modelType());
  }

  private E fullyPopulatedEntity() {
    return Instancio.of(entityType()).withSettings(ALL_FIELDS_SETTINGS).create();
  }

  @Test
  void toModel_copiesAuditFieldsFromEntity() {
    var entity = fullyPopulatedEntity();

    var model = mapper().toModel(entity);

    assertThat(model).isNotNull();
    assertMappedFields(mapper().toEntity(model), model);
  }

  @Test
  void toModelList_mapsEveryElement() {
    var entities = List.of(newEntity(), newEntity(), newEntity());

    var models = entities.stream().map(mapper()::toModel).toList();

    assertThat(models).hasSameSizeAs(entities);
    for (int i = 0; i < entities.size(); i++) {
      assertMappedFields(entities.get(i), models.get(i));
    }
  }

  @Test
  void toEntityList_mapsEveryElement() {
    var models = List.of(newModel(), newModel(), newModel());

    var entities = models.stream().map(mapper()::toEntity).toList();

    assertThat(entities).hasSameSizeAs(models);
    for (int i = 0; i < models.size(); i++) {
      assertMappedFields(entities.get(i), models.get(i));
    }
  }

  @Test
  void applyReplacement_updatesMappedFieldsOnly() {
    var entity = fullyPopulatedEntity();
    var idBefore = entity.getId();
    var createdAtBefore = entity.getCreatedAt();
    var updatedAtBefore = entity.getUpdatedAt();
    var versionBefore = entity.getVersion();

    var source = newModel();
    mapper().applyReplacement(source, entity);

    assertMappedFields(entity, source);
    assertThat(entity.getId()).isEqualTo(idBefore);
    assertThat(entity.getCreatedAt()).isEqualTo(createdAtBefore);
    assertThat(entity.getUpdatedAt()).isEqualTo(updatedAtBefore);
    assertThat(entity.getVersion()).isEqualTo(versionBefore);
  }

  @Test
  void roundTrip_preservesMappedFields() {
    var original = newEntity();

    var model = mapper().toModel(original);
    assertMappedFields(original, model);

    var backToEntity = mapper().toEntity(model);
    assertMappedFields(backToEntity, model);
  }
}
