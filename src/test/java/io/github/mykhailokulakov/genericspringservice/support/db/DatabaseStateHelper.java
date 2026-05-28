package io.github.mykhailokulakov.genericspringservice.support.db;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@TestComponent
public class DatabaseStateHelper {

  private final EntityManager em;

  public DatabaseStateHelper(EntityManager em) {
    this.em = em;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void truncateAll() {
    @SuppressWarnings("unchecked")
    List<String> tables =
        em.createNativeQuery(
                """
                SELECT tablename FROM pg_tables
                WHERE schemaname = 'public' AND tablename <> 'flyway_schema_history'
                """)
            .getResultList();
    if (tables.isEmpty()) {
      return;
    }
    var quoted = String.join(", ", tables.stream().map(t -> "\"" + t + "\"").toList());
    em.createNativeQuery("TRUNCATE TABLE " + quoted + " RESTART IDENTITY CASCADE").executeUpdate();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void truncate(String tableName) {
    var sanitized = sanitizeIdentifier(tableName);
    em.createNativeQuery("TRUNCATE TABLE \"" + sanitized + "\" RESTART IDENTITY CASCADE")
        .executeUpdate();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public long countIncludingDeleted(Class<?> entityClass) {
    String table = tableName(entityClass);
    var n =
        (Number) em.createNativeQuery("SELECT count(*) FROM \"" + table + "\"").getSingleResult();
    return n.longValue();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public long countWhereDeleted(Class<?> entityClass) {
    String table = tableName(entityClass);
    var n =
        (Number)
            em.createNativeQuery(
                    "SELECT count(*) FROM \"" + table + "\" WHERE deleted_at IS NOT NULL")
                .getSingleResult();
    return n.longValue();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean existsIncludingDeleted(Class<?> entityClass, UUID id) {
    String table = tableName(entityClass);
    var n =
        (Number)
            em.createNativeQuery("SELECT count(*) FROM \"" + table + "\" WHERE id = ?1")
                .setParameter(1, id)
                .getSingleResult();
    return n.longValue() > 0;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean isSoftDeleted(Class<?> entityClass, UUID id) {
    String table = tableName(entityClass);
    var n =
        (Number)
            em.createNativeQuery(
                    "SELECT count(*) FROM \""
                        + table
                        + "\" WHERE id = ?1 AND deleted_at IS NOT NULL")
                .setParameter(1, id)
                .getSingleResult();
    return n.longValue() > 0;
  }

  private static String sanitizeIdentifier(String name) {
    if (name == null || !name.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
      throw new IllegalArgumentException("Invalid SQL identifier: " + name);
    }
    return name;
  }

  private static String tableName(Class<?> entityClass) {
    var table = entityClass.getAnnotation(Table.class);
    if (table == null || table.name().isEmpty()) {
      throw new IllegalArgumentException(
          entityClass.getName()
              + " has no @Table(name = ...) annotation — DatabaseStateHelper requires an explicit"
              + " table name to avoid guessing the Hibernate naming strategy.");
    }
    return table.name();
  }
}
