package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.common.persistence.SoftDeletable;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FilterableRepository<E extends SoftDeletable>
    extends JpaRepository<E, UUID>, JpaSpecificationExecutor<E> {}
