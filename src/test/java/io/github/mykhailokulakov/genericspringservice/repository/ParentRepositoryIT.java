package io.github.mykhailokulakov.genericspringservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class ParentRepositoryIT extends AbstractRepositoryTestContract<ParentEntity> {

  @Autowired private EntityManager em;
  @Autowired private DatabaseStateHelper dbHelper;
  @Autowired private PlatformTransactionManager txManager;

  @Test
  void softDeleteParent_cascadesToChildren() {
    var parentId =
        new Object() {
          UUID value;
        };

    new TransactionTemplate(txManager)
        .executeWithoutResult(
            status -> {
              var parent = ParentEntity.builder().label("parent").build();
              var child = ChildEntity.builder().value("child").parent(parent).build();
              parent.setChildren(List.of(child));
              em.persist(parent);
              em.flush();
              parentId.value = parent.getId();
            });

    new TransactionTemplate(txManager)
        .executeWithoutResult(
            status -> {
              var loadedParent = em.find(ParentEntity.class, parentId.value);
              em.remove(loadedParent);
              em.flush();
            });

    assertThat(dbHelper.countIncludingDeleted(ParentEntity.class)).isOne();
    assertThat(dbHelper.countWhereDeleted(ParentEntity.class)).isOne();
    assertThat(dbHelper.countIncludingDeleted(ChildEntity.class)).isOne();
    assertThat(dbHelper.countWhereDeleted(ChildEntity.class))
        .as("cascade soft-delete: child is also soft-deleted when parent is")
        .isOne();
  }
}
