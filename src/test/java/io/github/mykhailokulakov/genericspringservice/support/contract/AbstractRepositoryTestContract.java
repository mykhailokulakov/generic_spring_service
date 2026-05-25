package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.support.PersistenceTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import jakarta.persistence.EntityManager;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;
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

  protected abstract void mutate(E entity);

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
  void softDeleteParent_doesNotOrphanChildren() {
    var parentRef =
        new Object() {
          UUID parentId;
          UUID childId;
        };

    tx().executeWithoutResult(
            status -> {
              var parent = ParentEntity.builder().label("parent").build();
              var child = ChildEntity.builder().value("child").parent(parent).build();
              parent.setChildren(List.of(child));
              em.persist(parent);
              em.flush();
              parentRef.parentId = parent.getId();
              parentRef.childId = child.getId();
            });

    tx().executeWithoutResult(
            status -> {
              var loadedParent = em.find(ParentEntity.class, parentRef.parentId);
              em.remove(loadedParent);
              em.flush();
            });

    assertThat(dbHelper.countIncludingDeleted(ParentEntity.class)).isOne();
    assertThat(dbHelper.countWhereDeleted(ParentEntity.class)).isOne();
    assertThat(dbHelper.countIncludingDeleted(ChildEntity.class)).isOne();
    assertThat(dbHelper.countWhereDeleted(ChildEntity.class)).isZero();

    tx().executeWithoutResult(
            status -> {
              var childReloaded = em.find(ChildEntity.class, parentRef.childId);
              assertThat(childReloaded).isNotNull();
              assertThat(childReloaded.getParent())
                  .as("soft-deleted parent resolves to null via @NotFound(IGNORE)")
                  .isNull();
            });
  }

  @Test
  void manyToOne_foreignKeySurvivesReload() {
    var ids =
        new Object() {
          UUID parentId;
          UUID childId;
        };

    tx().executeWithoutResult(
            status -> {
              var parent = ParentEntity.builder().label("p").build();
              em.persist(parent);
              var child = ChildEntity.builder().value("c").parent(parent).build();
              em.persist(child);
              em.flush();
              ids.parentId = parent.getId();
              ids.childId = child.getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em.find(ChildEntity.class, ids.childId);
              assertThat(reloaded.getParent().getId()).isEqualTo(ids.parentId);
            });
  }

  @Test
  void oneToOne_foreignKeySurvivesReload() {
    var ids =
        new Object() {
          UUID ownerId;
          UUID exampleId;
        };

    tx().executeWithoutResult(
            status -> {
              var example =
                  ExampleEntity.builder()
                      .name("test")
                      .status(ExampleStatus.ACTIVE)
                      .price(BigDecimal.ONE)
                      .build();
              em.persist(example);
              var owner = OwnerEntity.builder().handle("owner").example(example).build();
              em.persist(owner);
              em.flush();
              ids.ownerId = owner.getId();
              ids.exampleId = example.getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em.find(OwnerEntity.class, ids.ownerId);
              assertThat(reloaded.getExample()).isNotNull();
              assertThat(reloaded.getExample().getId()).isEqualTo(ids.exampleId);
            });
  }

  @Test
  void manyToMany_associationSurvivesReload() {
    var ids =
        new Object() {
          UUID leftId;
          UUID rightId;
        };

    tx().executeWithoutResult(
            status -> {
              var right = RightEntity.builder().name("right").build();
              em.persist(right);
              var left = LeftEntity.builder().code("left").rights(Set.of(right)).build();
              em.persist(left);
              em.flush();
              ids.leftId = left.getId();
              ids.rightId = right.getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em.find(LeftEntity.class, ids.leftId);
              assertThat(reloaded.getRights()).hasSize(1);
              assertThat(reloaded.getRights().iterator().next().getId()).isEqualTo(ids.rightId);
            });
  }
}
