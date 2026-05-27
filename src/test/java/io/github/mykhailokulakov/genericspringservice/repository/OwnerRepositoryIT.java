package io.github.mykhailokulakov.genericspringservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.domain.entity.OwnerEntity;
import io.github.mykhailokulakov.genericspringservice.domain.model.ExampleStatus;
import io.github.mykhailokulakov.genericspringservice.support.contract.AbstractRepositoryTestContract;
import io.github.mykhailokulakov.genericspringservice.support.fixtures.RandomEntities;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class OwnerRepositoryIT extends AbstractRepositoryTestContract<OwnerEntity> {

  @Autowired private ExampleRepository exampleRepository;
  @Autowired private EntityManager em;
  @Autowired private PlatformTransactionManager txManager;

  @Override
  protected OwnerEntity newEntity() {
    var example = exampleRepository.saveAndFlush(RandomEntities.create(ExampleEntity.class));
    var owner = RandomEntities.create(OwnerEntity.class);
    owner.setExample(example);
    return owner;
  }

  @Test
  void oneToOne_foreignKeySurvivesReload() {
    var ids =
        new Object() {
          UUID ownerId;
          UUID exampleId;
        };

    new TransactionTemplate(txManager)
        .executeWithoutResult(
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

    new TransactionTemplate(txManager)
        .executeWithoutResult(
            status -> {
              var reloaded = em.find(OwnerEntity.class, ids.ownerId);
              assertThat(reloaded.getExample()).isNotNull();
              assertThat(reloaded.getExample().getId()).isEqualTo(ids.exampleId);
            });
  }
}
