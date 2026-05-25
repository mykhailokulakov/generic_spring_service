package io.github.mykhailokulakov.genericspringservice.support.contract;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.support.IntegrationTest;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RepoFixture;
import io.github.mykhailokulakov.genericspringservice.support.testentities.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.support.testentities.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.support.testentities.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.support.testentities.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.support.testentities.ProfileEntity;
import io.github.mykhailokulakov.genericspringservice.support.testentities.RightEntity;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.support.Repositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

@IntegrationTest
public abstract class AbstractRepositoryContractIT<E extends SoftDeletable> {

  @LocalServerPort private int port;

  @Autowired private WebApplicationContext applicationContext;
  @Autowired private EntityManager em;
  @Autowired private DatabaseStateHelper dbHelper;
  @Autowired private PlatformTransactionManager txManager;

  private JpaRepository<E, ?> cachedRepository;

  protected abstract RepoFixture<E> fixture();

  @SuppressWarnings("unchecked")
  private JpaRepository<E, ?> repository() {
    if (cachedRepository == null) {
      cachedRepository =
          (JpaRepository<E, ?>)
              new Repositories(applicationContext)
                  .getRepositoryFor(fixture().entityType())
                  .orElseThrow();
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
    var repo = repository();
    var saved = repo.saveAndFlush(entity);
    em.clear();
    return saved;
  }

  @Test
  void save_assignsIdAndAuditTimestamps() {
    var entity = fixture().newPersistable();

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
    var saved = persistAndFlush(fixture().newPersistable());

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    var found = repo.findById(saved.getId());

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
    assertThat(found.get().getCreatedAt()).isEqualTo(saved.getCreatedAt());
  }

  @Test
  void update_incrementsVersion() {
    var saved = persistAndFlush(fixture().newPersistable());
    var initialVersion = saved.getVersion();

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    var reloaded = repo.findById(saved.getId()).orElseThrow();
    fixture().mutate(reloaded);
    repo.saveAndFlush(reloaded);
    em.clear();

    var updated = repo.findById(saved.getId()).orElseThrow();
    assertThat(updated.getVersion()).isGreaterThan(initialVersion);
  }

  @Test
  void delete_softDeletesAndExcludesFromQueries() {
    var saved = persistAndFlush(fixture().newPersistable());

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    repo.deleteById(saved.getId());
    repo.flush();
    em.clear();

    assertThat(repo.findById(saved.getId())).isEmpty();
    assertThat(repo.findAll()).isEmpty();
  }

  @Test
  void delete_keepsRowInTable() {
    var saved = persistAndFlush(fixture().newPersistable());

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    repo.deleteById(saved.getId());
    repo.flush();
    em.clear();

    assertThat(dbHelper.countIncludingDeleted(fixture().entityType())).isOne();
    assertThat(dbHelper.countWhereDeleted(fixture().entityType())).isOne();
  }

  @Test
  void count_excludesSoftDeleted() {
    persistAndFlush(fixture().newPersistable());
    var toDelete = persistAndFlush(fixture().newPersistable());

    @SuppressWarnings("unchecked")
    var repo = (JpaRepository<E, Object>) repository();
    repo.deleteById(toDelete.getId());
    repo.flush();
    em.clear();

    assertThat(repo.count()).isOne();
    assertThat(dbHelper.countIncludingDeleted(fixture().entityType())).isEqualTo(2);
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
              assertThat(childReloaded.getParent().getId()).isEqualTo(parentRef.parentId);
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
          UUID profileId;
        };

    tx().executeWithoutResult(
            status -> {
              var profile = ProfileEntity.builder().bio("test bio").build();
              var owner = OwnerEntity.builder().handle("owner").profile(profile).build();
              em.persist(owner);
              em.flush();
              ids.ownerId = owner.getId();
              ids.profileId = profile.getId();
            });

    tx().executeWithoutResult(
            status -> {
              var reloaded = em.find(OwnerEntity.class, ids.ownerId);
              assertThat(reloaded.getProfile()).isNotNull();
              assertThat(reloaded.getProfile().getId()).isEqualTo(ids.profileId);
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
