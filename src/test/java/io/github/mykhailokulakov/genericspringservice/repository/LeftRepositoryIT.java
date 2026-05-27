package io.github.mykhailokulakov.genericspringservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import jakarta.persistence.EntityManager;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class LeftRepositoryIT extends AbstractRepositoryTestContract<LeftEntity> {

  @Autowired private EntityManager em;
  @Autowired private PlatformTransactionManager txManager;

  @Test
  void manyToMany_associationSurvivesReload() {
    var ids =
        new Object() {
          UUID leftId;
          UUID rightId;
        };

    new TransactionTemplate(txManager)
        .executeWithoutResult(
            status -> {
              var right = RightEntity.builder().name("right").build();
              em.persist(right);
              var left = LeftEntity.builder().code("left").rights(Set.of(right)).build();
              em.persist(left);
              em.flush();
              ids.leftId = left.getId();
              ids.rightId = right.getId();
            });

    new TransactionTemplate(txManager)
        .executeWithoutResult(
            status -> {
              var reloaded = em.find(LeftEntity.class, ids.leftId);
              assertThat(reloaded.getRights()).hasSize(1);
              assertThat(reloaded.getRights().iterator().next().getId()).isEqualTo(ids.rightId);
            });
  }
}
