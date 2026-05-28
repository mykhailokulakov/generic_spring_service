package io.github.mykhailokulakov.genericspringservice.support.contract;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
import java.util.function.Predicate;

final class ContractReflection {

  private ContractReflection() {}

  static Optional<Field> findField(Class<?> type, Predicate<Field> predicate) {
    var current = type;
    while (current != null && current != Object.class) {
      for (var f : current.getDeclaredFields()) {
        if (predicate.test(f)) {
          return Optional.of(f);
        }
      }
      current = current.getSuperclass();
    }
    return Optional.empty();
  }

  static Optional<Field> findFirstField(Class<?> type, Class<? extends Annotation> annotation) {
    return findField(type, f -> f.isAnnotationPresent(annotation));
  }

  static Optional<Field> findOwningSide(Class<?> type, Class<? extends Annotation> annotation) {
    return findField(type, f -> f.isAnnotationPresent(annotation) && !hasMappedBy(f, annotation));
  }

  static Optional<Field> findBackReference(Class<?> childType, Class<?> parentType) {
    return findField(
        childType, f -> f.isAnnotationPresent(ManyToOne.class) && f.getType().equals(parentType));
  }

  static Class<?> collectionElementType(Field field) {
    var generic = (ParameterizedType) field.getGenericType();
    return (Class<?>) generic.getActualTypeArguments()[0];
  }

  static boolean hasCascadeRemove(OneToMany annotation) {
    for (var ct : annotation.cascade()) {
      if (ct == CascadeType.REMOVE || ct == CascadeType.ALL) {
        return true;
      }
    }
    return false;
  }

  static void setField(Object target, Field field, Object value) {
    field.setAccessible(true);
    try {
      field.set(target, value);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  static Object getField(Object target, Field field) {
    field.setAccessible(true);
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  private static boolean hasMappedBy(Field field, Class<? extends Annotation> annotation) {
    if (annotation == OneToOne.class) {
      return !field.getAnnotation(OneToOne.class).mappedBy().isEmpty();
    }
    if (annotation == ManyToMany.class) {
      return !field.getAnnotation(ManyToMany.class).mappedBy().isEmpty();
    }
    return false;
  }
}
