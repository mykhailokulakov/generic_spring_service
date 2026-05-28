package io.github.mykhailokulakov.genericspringservice.support.contract;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import io.github.mykhailokulakov.genericspringservice.support.db.DatabaseStateHelper;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.support.TransactionTemplate;

public interface RepositoryContractAccess<E extends SoftDeletable> {

  EntityManager em();

  TransactionTemplate tx();

  Class<E> entityType();

  E newEntity();

  DatabaseStateHelper dbHelper();
}
