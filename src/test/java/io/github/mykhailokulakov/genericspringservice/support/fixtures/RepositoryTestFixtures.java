package io.github.mykhailokulakov.genericspringservice.support.fixtures;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;

@TestComponent
public class RepositoryTestFixtures {

  private final ApplicationContext applicationContext;
  private Repositories repositories;

  public RepositoryTestFixtures(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  public <T> T newOf(Class<T> type) {
    return newOf(type, new HashSet<>());
  }

  private <T> T newOf(Class<T> type, Set<Class<?>> inProgress) {
    if (inProgress.contains(type)) {
      throw new IllegalStateException(
          "Cyclic required association detected while building "
              + type.getSimpleName()
              + "; required-FK chain re-enters this type");
    }
    var entity = RandomEntities.create(type);
    var nextInProgress = new HashSet<>(inProgress);
    nextInProgress.add(type);
    for (var field : requiredOwningAssociationFields(type)) {
      var parentType = field.getType();
      var parent = newOf(parentType, nextInProgress);
      var savedParent = persist(parentType, parent);
      setField(entity, field, savedParent);
    }
    return entity;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Object persist(Class<?> type, Object entity) {
    if (repositories == null) {
      repositories = new Repositories(applicationContext);
    }
    var repo =
        (JpaRepository)
            repositories
                .getRepositoryFor(type)
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "No JpaRepository bean exposes " + type.getName()));
    return repo.saveAndFlush(entity);
  }

  private static List<Field> requiredOwningAssociationFields(Class<?> type) {
    var result = new ArrayList<Field>();
    var current = type;
    while (current != null && current != Object.class) {
      for (var f : current.getDeclaredFields()) {
        if (isRequiredOwningAssociation(f)) {
          result.add(f);
        }
      }
      current = current.getSuperclass();
    }
    return result;
  }

  private static boolean isRequiredOwningAssociation(Field f) {
    if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) {
      return false;
    }
    var manyToOne = f.getAnnotation(ManyToOne.class);
    if (manyToOne != null) {
      return !manyToOne.optional() || joinColumnNotNullable(f);
    }
    var oneToOne = f.getAnnotation(OneToOne.class);
    if (oneToOne != null && oneToOne.mappedBy().isEmpty()) {
      return !oneToOne.optional() || joinColumnNotNullable(f);
    }
    return false;
  }

  private static boolean joinColumnNotNullable(Field f) {
    var jc = f.getAnnotation(JoinColumn.class);
    return jc != null && !jc.nullable();
  }

  private static void setField(Object target, Field field, Object value) {
    field.setAccessible(true);
    try {
      field.set(target, value);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }
}
