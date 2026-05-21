package io.github.mykhailokulakov.genericspringservice.support.fixtures;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import io.github.mykhailokulakov.genericspringservice.repository.ExampleRepository;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

public class SeededExamplesExtension implements BeforeEachCallback {

  @Override
  public void beforeEach(ExtensionContext ctx) {
    WithSeededExamples annotation =
        ctx.getRequiredTestClass().getAnnotation(WithSeededExamples.class);
    if (annotation == null) {
      return;
    }

    ApplicationContext appCtx = SpringExtension.getApplicationContext(ctx);
    ExampleRepository repository = appCtx.getBean(ExampleRepository.class);

    if (annotation.truncate()) {
      truncateExampleTables(appCtx);
    }

    Set<String> annotationTags = new HashSet<>(Arrays.asList(annotation.tags()));
    for (int i = 0; i < annotation.count(); i++) {
      ExampleEntity entity = ExampleFixtures.randomActive();
      if (!annotationTags.isEmpty()) {
        entity.getTags().addAll(annotationTags);
      }
      repository.save(entity);
    }
    repository.flush();
  }

  private static void truncateExampleTables(ApplicationContext appCtx) {
    try {
      DatabaseStateHelper helper = appCtx.getBean(DatabaseStateHelper.class);
      helper.truncate("example_tag");
      helper.truncate("example");
      return;
    } catch (NoSuchBeanDefinitionException ignored) {
      // Fall through to TransactionTemplate path below.
    }
    EntityManager em = appCtx.getBean(EntityManager.class);
    TransactionTemplate tx =
        new TransactionTemplate(appCtx.getBean(PlatformTransactionManager.class));
    tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    tx.executeWithoutResult(
        status ->
            em.createNativeQuery(
                    "TRUNCATE TABLE \"example_tag\", \"example\" RESTART IDENTITY CASCADE")
                .executeUpdate());
  }
}
