package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.model.DomainModel;
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

public abstract class AbstractMapperContractIT<E extends SoftDeletable, M extends DomainModel> {

  private static final Settings ALL_FIELDS_SETTINGS =
      Settings.create()
          .set(Keys.ASSIGNMENT_TYPE, AssignmentType.FIELD)
          .set(Keys.ON_SET_FIELD_ERROR, OnSetFieldError.IGNORE)
          .lock();

  private Class<E> entityType;
  private Class<M> modelType;

  protected abstract EntityMapper<E, M> mapper();

  protected abstract void assertDomainFields(E entity, M model);

  @SuppressWarnings("unchecked")
  protected Class<E> entityType() {
    if (entityType == null) {
      var superclass = (ParameterizedType) getClass().getGenericSuperclass();
      entityType = (Class<E>) superclass.getActualTypeArguments()[0];
    }
    return entityType;
  }

  @SuppressWarnings("unchecked")
  protected Class<M> modelType() {
    if (modelType == null) {
      var superclass = (ParameterizedType) getClass().getGenericSuperclass();
      modelType = (Class<M>) superclass.getActualTypeArguments()[1];
    }
    return modelType;
  }

  protected E newEntity() {
    return RandomEntities.create(entityType());
  }

  protected M newModel() {
    return RandomModels.create(modelType());
  }

  private E fullyPopulatedEntity() {
    return Instancio.of(entityType()).withSettings(ALL_FIELDS_SETTINGS).create();
  }

  private void assertChainFields(E entity, M model) {
    assertThat(model.id()).isEqualTo(entity.getId());
    assertThat(model.createdAt()).isEqualTo(entity.getCreatedAt());
    assertThat(model.updatedAt()).isEqualTo(entity.getUpdatedAt());
    assertThat(model.version()).isEqualTo(entity.getVersion());
  }

  @Test
  void toModel_mapsAllFields() {
    var entity = fullyPopulatedEntity();

    var model = mapper().toModel(entity);

    assertChainFields(entity, model);
    assertDomainFields(entity, model);
  }

  @Test
  void toModelList_mapsEveryElement() {
    var entities = List.of(newEntity(), newEntity(), newEntity());

    var models = entities.stream().map(mapper()::toModel).toList();

    assertThat(models).hasSameSizeAs(entities);
    for (int i = 0; i < entities.size(); i++) {
      assertDomainFields(entities.get(i), models.get(i));
    }
  }

  @Test
  void toEntityList_mapsEveryElement() {
    var models = List.of(newModel(), newModel(), newModel());

    var entities = models.stream().map(mapper()::toEntity).toList();

    assertThat(entities).hasSameSizeAs(models);
    for (int i = 0; i < models.size(); i++) {
      assertDomainFields(entities.get(i), models.get(i));
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

    assertDomainFields(entity, source);
    assertThat(entity.getId()).isEqualTo(idBefore);
    assertThat(entity.getCreatedAt()).isEqualTo(createdAtBefore);
    assertThat(entity.getUpdatedAt()).isEqualTo(updatedAtBefore);
    assertThat(entity.getVersion()).isEqualTo(versionBefore);
  }

  @Test
  void roundTrip_preservesAllFields() {
    var entity = fullyPopulatedEntity();

    var model = mapper().toModel(entity);
    assertChainFields(entity, model);
    assertDomainFields(entity, model);

    var backToEntity = mapper().toEntity(model);
    assertChainFields(backToEntity, model);
    assertDomainFields(backToEntity, model);
  }
}
