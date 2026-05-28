package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.support.PersistenceTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
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
public abstract class AbstractRepositoryTestContract<E extends SoftDeletable>
    implements RepositoryContractAccess<E> {

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

  @Override
  @SuppressWarnings("unchecked")
  public Class<E> entityType() {
    if (resolvedEntityType == null) {
      var superclass = (ParameterizedType) getClass().getGenericSuperclass();
      resolvedEntityType = (Class<E>) superclass.getActualTypeArguments()[0];
    }
    return resolvedEntityType;
  }

  @Override
  public EntityManager em() {
    return em;
  }

  @Override
  public TransactionTemplate tx() {
    return new TransactionTemplate(txManager);
  }

  @Override
  public E newEntity() {
    return RandomEntities.create(entityType());
  }

  @Override
  public DatabaseStateHelper dbHelper() {
    return dbHelper;
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

  @BeforeEach
  void contractCleanDb() {
    dbHelper.truncateAll();
  }

  private E persistAndFlush(E entity) {
    return repository().saveAndFlush(entity);
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
}
