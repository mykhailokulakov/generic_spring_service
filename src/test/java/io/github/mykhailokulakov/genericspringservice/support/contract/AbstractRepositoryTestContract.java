package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.mykhailokulakov.genericspringservice.common.persistence.Identifiable;
import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.support.PersistenceTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.support.Repositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@PersistenceTest
@Import(DatabaseStateHelper.class)
public abstract class AbstractRepositoryTestContract<E extends SoftDeletable> {

  @Autowired private ApplicationContext applicationContext;
  @Autowired private EntityManager em;
  @Autowired private DatabaseStateHelper dbHelper;
  @Autowired private PlatformTransactionManager txManager;

  private Class<E> resolvedEntityType;
  private JpaRepository<E, ?> cachedRepository;
  private JpaSpecificationExecutor<E> cachedSpecExecutor;

  private void mutate(E entity) {
    var field =
        Arrays.stream(entityType().getDeclaredFields())
            .filter(AbstractRepositoryTestContract::isMutableValueField)
            .findFirst()
            .orElseThrow();
    field.setAccessible(true);
    try {
      field.set(entity, Instancio.create(field.getType()));
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  private static boolean isMutableValueField(Field field) {
    return !field.isSynthetic()
        && !Modifier.isStatic(field.getModifiers())
        && !field.isAnnotationPresent(ManyToOne.class)
        && !field.isAnnotationPresent(OneToOne.class)
        && !field.isAnnotationPresent(OneToMany.class)
        && !field.isAnnotationPresent(ManyToMany.class)
        && !field.isAnnotationPresent(ElementCollection.class);
  }

  @SuppressWarnings("unchecked")
  private Class<E> entityType() {
    if (resolvedEntityType == null) {
      var superclass = (ParameterizedType) getClass().getGenericSuperclass();
      resolvedEntityType = (Class<E>) superclass.getActualTypeArguments()[0];
    }
    return resolvedEntityType;
  }

  @SuppressWarnings("unchecked")
  private JpaRepository<E, ?> repository() {
    if (cachedRepository == null) {
      cachedRepository =
          (JpaRepository<E, ?>)
              new Repositories(applicationContext).getRepositoryFor(entityType()).orElseThrow();
    }
    return cachedRepository;
  }

  @SuppressWarnings("unchecked")
  private JpaSpecificationExecutor<E> specExecutor() {
    if (cachedSpecExecutor == null) {
      cachedSpecExecutor = (JpaSpecificationExecutor<E>) repository();
    }
    return cachedSpecExecutor;
  }

  private TransactionTemplate tx() {
    return new TransactionTemplate(txManager);
  }

  @BeforeEach
  void contractCleanDb() {
    dbHelper.truncateAll();
  }

  private E persistAndFlush(E entity) {
    return repository().saveAndFlush(entity);
  }

  protected E newEntity() {
    return RandomEntities.create(entityType());
  }

  private static Optional<Field> findFirstField(
      Class<?> type, Class<? extends java.lang.annotation.Annotation> annotation) {
    return Arrays.stream(type.getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(annotation))
        .findFirst();
  }

  private static Optional<Field> findOwningSide(
      Class<?> type, Class<? extends java.lang.annotation.Annotation> annotation) {
    return Arrays.stream(type.getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(annotation))
        .filter(f -> !hasMappedBy(f, annotation))
        .findFirst();
  }

  private static boolean hasMappedBy(
      Field field, Class<? extends java.lang.annotation.Annotation> annotation) {
    if (annotation == OneToOne.class) {
      return !field.getAnnotation(OneToOne.class).mappedBy().isEmpty();
    }
    if (annotation == ManyToMany.class) {
      return !field.getAnnotation(ManyToMany.class).mappedBy().isEmpty();
    }
    return false;
  }

  private static Class<?> collectionElementType(Field field) {
    var generic = (ParameterizedType) field.getGenericType();
    return (Class<?>) generic.getActualTypeArguments()[0];
  }

  private static void setField(Object target, Field field, Object value) {
    field.setAccessible(true);
    try {
      field.set(target, value);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  private static Object getField(Object target, Field field) {
    field.setAccessible(true);
    try {
      return field.get(target);
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
  }

  private static Optional<Field> findBackReference(Class<?> childType, Class<?> parentType) {
    return Arrays.stream(childType.getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(ManyToOne.class))
        .filter(f -> f.getType().equals(parentType))
        .findFirst();
  }

  private static boolean hasCascadeRemove(OneToMany annotation) {
    for (var ct : annotation.cascade()) {
      if (ct == CascadeType.REMOVE || ct == CascadeType.ALL) {
        return true;
      }
    }
    return false;
  }

  @Test
  void save_assignsIdAndAuditTimestamps() {
    var entity = newEntity();

    assertThat(entity.getId()).isNull();
    assertThat(entity.getCreatedAt()).isNull();
    assertThat(entity.getUpdatedAt()).isNull();
    assertThat(entity.getVersion()).isNull();

    var saved = persistAndFlush(entity);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
    assertThat(saved.getVersion()).isNotNull();
    assertThat(saved.getVersion()).isZero();
  }

  @Test
  void findById_returnsPersistedEntity() {
    var saved = persistAndFlush(newEntity());

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    var found = repo.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
    assertThat(found.get().getCreatedAt())
        .isCloseTo(saved.getCreatedAt(), within(Duration.ofNanos(1000)));
  }

  @Test
  void findById_returnsEmptyForNonExistentId() {
    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();

    assertThat(repo.findById(UUID.randomUUID())).isEmpty();
  }

  @Test
  void update_incrementsVersion() {
    var saved = persistAndFlush(newEntity());
    var initialVersion = saved.getVersion();

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    var reloaded = repo.findById(saved.getId()).orElseThrow();
    mutate(reloaded);
    repo.saveAndFlush(reloaded);

    var updated = repo.findById(saved.getId()).orElseThrow();
    assertThat(updated.getVersion()).isGreaterThan(initialVersion);
  }

  @Test
  void update_advancesUpdatedAt() {
    var saved = persistAndFlush(newEntity());
    var originalUpdatedAt = saved.getUpdatedAt();

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    var reloaded = repo.findById(saved.getId()).orElseThrow();
    mutate(reloaded);
    repo.saveAndFlush(reloaded);

    var updated = repo.findById(saved.getId()).orElseThrow();
    assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
  }

  @Test
  void update_doesNotChangeCreatedAt() {
    var saved = persistAndFlush(newEntity());
    var originalCreatedAt = saved.getCreatedAt();

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    var reloaded = repo.findById(saved.getId()).orElseThrow();
    mutate(reloaded);
    repo.saveAndFlush(reloaded);

    var updated = repo.findById(saved.getId()).orElseThrow();
    assertThat(updated.getCreatedAt()).isCloseTo(originalCreatedAt, within(Duration.ofNanos(1000)));
  }

  @Test
  void update_rejectsStaleVersion() {
    var saved = persistAndFlush(newEntity());

    tx().executeWithoutResult(
            status -> {
              var current = em.find(entityType(), saved.getId());
              mutate(current);
              em.flush();
            });

    mutate(saved);
    assertThatThrownBy(() -> repository().saveAndFlush(saved))
        .isInstanceOf(OptimisticLockingFailureException.class);
  }

  @Test
  void delete_softDeletesAndExcludesFromQueries() {
    var saved = persistAndFlush(newEntity());

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    repo.deleteById(saved.getId());
    repo.flush();

    assertThat(repo.findById(saved.getId())).isEmpty();
    assertThat(repo.findAll()).isEmpty();
  }

  @Test
  void delete_keepsRowInTable() {
    var saved = persistAndFlush(newEntity());

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    repo.deleteById(saved.getId());
    repo.flush();

    assertThat(dbHelper.countIncludingDeleted(entityType())).isOne();
    assertThat(dbHelper.countWhereDeleted(entityType())).isOne();
  }

  @Test
  void findAll_paginatesAndSorts() {
    for (int i = 0; i < 5; i++) {
      persistAndFlush(newEntity());
    }

    var page = repository().findAll(PageRequest.of(0, 3, Sort.by("createdAt")));

    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getTotalPages()).isEqualTo(2);

    var secondPage = repository().findAll(PageRequest.of(1, 3, Sort.by("createdAt")));

    assertThat(secondPage.getContent()).hasSize(2);
  }

  @Test
  void specification_emptyReturnsAll() {
    persistAndFlush(newEntity());
    persistAndFlush(newEntity());
    persistAndFlush(newEntity());

    var page = specExecutor().findAll(Specification.unrestricted(), Pageable.unpaged());

    assertThat(page.getContent()).hasSize(3);
  }

  @Test
  void specification_paginatesAndSorts() {
    for (int i = 0; i < 5; i++) {
      persistAndFlush(newEntity());
    }

    var page =
        specExecutor()
            .findAll(Specification.unrestricted(), PageRequest.of(0, 3, Sort.by("createdAt")));

    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getContent()).hasSize(3);
    assertThat(page.getTotalPages()).isEqualTo(2);
  }

  @Test
  void specification_excludesSoftDeleted() {
    persistAndFlush(newEntity());
    var toDelete = persistAndFlush(newEntity());

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    repo.deleteById(toDelete.getId());
    repo.flush();

    var page = specExecutor().findAll(Specification.unrestricted(), Pageable.unpaged());

    assertThat(page.getTotalElements()).isOne();
  }

  @Test
  void count_excludesSoftDeleted() {
    persistAndFlush(newEntity());
    var toDelete = persistAndFlush(newEntity());

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    repo.deleteById(toDelete.getId());
    repo.flush();

    assertThat(repo.count()).isOne();
    assertThat(dbHelper.countIncludingDeleted(entityType())).isEqualTo(2);
  }

  @Test
  void manyToOne_foreignKeySurvivesReload() {
    var field = findFirstField(entityType(), ManyToOne.class);
    assumeTrue(field.isPresent(), "entity has no @ManyToOne field");

    var manyToOneField = field.get();
    var targetType = manyToOneField.getType();

    var ids =
        new Object() {
          UUID entityId;
          UUID targetId;
        };

    tx().executeWithoutResult(
            status -> {
              var target = RandomEntities.create(targetType);
              em.persist(target);
              var entity = newEntity();
              setField(entity, manyToOneField, target);
              em.persist(entity);
              em.flush();
              ids.entityId = entity.getId();
              ids.targetId = ((Identifiable) target).getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em.find(entityType(), ids.entityId);
              var target = (Identifiable) getField(reloaded, manyToOneField);
              assertThat(target.getId()).isEqualTo(ids.targetId);
            });
  }

  @Test
  void oneToOne_foreignKeySurvivesReload() {
    var field = findOwningSide(entityType(), OneToOne.class);
    assumeTrue(field.isPresent(), "entity has no owning @OneToOne field");

    var oneToOneField = field.get();
    var targetType = oneToOneField.getType();

    var ids =
        new Object() {
          UUID entityId;
          UUID targetId;
        };

    tx().executeWithoutResult(
            status -> {
              var target = RandomEntities.create(targetType);
              em.persist(target);
              var entity = newEntity();
              setField(entity, oneToOneField, target);
              em.persist(entity);
              em.flush();
              ids.entityId = entity.getId();
              ids.targetId = ((Identifiable) target).getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em.find(entityType(), ids.entityId);
              var target = (Identifiable) getField(reloaded, oneToOneField);
              assertThat(target).isNotNull();
              assertThat(target.getId()).isEqualTo(ids.targetId);
            });
  }

  @Test
  void manyToMany_associationSurvivesReload() {
    var field = findOwningSide(entityType(), ManyToMany.class);
    assumeTrue(field.isPresent(), "entity has no owning @ManyToMany field");

    var manyToManyField = field.get();
    var targetType = collectionElementType(manyToManyField);

    var ids =
        new Object() {
          UUID entityId;
          UUID targetId;
        };

    tx().executeWithoutResult(
            status -> {
              var target = RandomEntities.create(targetType);
              em.persist(target);
              var entity = newEntity();
              setField(entity, manyToManyField, Set.of(target));
              em.persist(entity);
              em.flush();
              ids.entityId = entity.getId();
              ids.targetId = ((Identifiable) target).getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em.find(entityType(), ids.entityId);
              var targets = (Collection<?>) getField(reloaded, manyToManyField);
              assertThat(targets).hasSize(1);
              assertThat(((Identifiable) targets.iterator().next()).getId())
                  .isEqualTo(ids.targetId);
            });
  }

  @SuppressWarnings("unchecked")
  @Test
  void softDeleteParent_cascadesToChildren() {
    var oneToManyField =
        Arrays.stream(entityType().getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(OneToMany.class))
            .filter(f -> hasCascadeRemove(f.getAnnotation(OneToMany.class)))
            .findFirst();
    assumeTrue(oneToManyField.isPresent(), "entity has no @OneToMany with cascade REMOVE");

    var collectionField = oneToManyField.get();
    var childType = (Class<? extends Identifiable>) collectionElementType(collectionField);
    var backRef = findBackReference(childType, entityType());
    assumeTrue(backRef.isPresent(), "child has no @ManyToOne back-reference to parent");

    var backRefField = backRef.get();

    var parentId =
        new Object() {
          UUID value;
        };

    tx().executeWithoutResult(
            status -> {
              var parent = newEntity();
              em.persist(parent);
              em.flush();

              var child = RandomEntities.create(childType);
              setField(child, backRefField, parent);
              em.persist(child);

              var children = new HashSet<>();
              children.add(child);
              setField(parent, collectionField, children);
              em.flush();

              parentId.value = parent.getId();
            });

    tx().executeWithoutResult(
            status -> {
              var loaded = em.find(entityType(), parentId.value);
              em.remove(loaded);
              em.flush();
            });

    assertThat(dbHelper.countIncludingDeleted(entityType())).isOne();
    assertThat(dbHelper.countWhereDeleted(entityType())).isOne();
    assertThat(dbHelper.countIncludingDeleted(childType)).isOne();
    assertThat(dbHelper.countWhereDeleted(childType))
        .as("cascade soft-delete: child is also soft-deleted when parent is")
        .isOne();
  }
}
