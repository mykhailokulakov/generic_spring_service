package io.github.mykhailokulakov.genericspringservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class ChildRepositoryIT extends AbstractRepositoryTestContract<ChildEntity> {

  @Autowired private ParentRepository parentRepository;
  @Autowired private EntityManager em;
  @Autowired private PlatformTransactionManager txManager;

  @Override
  protected ChildEntity newEntity() {
    var parent = parentRepository.saveAndFlush(RandomEntities.create(ParentEntity.class));
    var child = RandomEntities.create(ChildEntity.class);
    child.setParent(parent);
    return child;
  }

  @Test
  void manyToOne_foreignKeySurvivesReload() {
    var ids =
        new Object() {
          UUID parentId;
          UUID childId;
        };

    new TransactionTemplate(txManager)
        .executeWithoutResult(
            status -> {
              var parent = ParentEntity.builder().label("p").build();
              em.persist(parent);
              var child = ChildEntity.builder().value("c").parent(parent).build();
              em.persist(child);
              em.flush();
              ids.parentId = parent.getId();
              ids.childId = child.getId();
            });

    new TransactionTemplate(txManager)
        .executeWithoutResult(
            status -> {
              var reloaded = em.find(ChildEntity.class, ids.childId);
              assertThat(reloaded.getParent().getId()).isEqualTo(ids.parentId);
            });
  }
}
