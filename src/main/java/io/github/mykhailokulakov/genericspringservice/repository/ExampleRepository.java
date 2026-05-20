package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExampleRepository
    extends JpaRepository<ExampleEntity, UUID>, JpaSpecificationExecutor<ExampleEntity> {

  @EntityGraph(attributePaths = "tags")
  @Override
  Page<ExampleEntity> findAll(Specification<ExampleEntity> spec, Pageable pageable);

  @EntityGraph(attributePaths = "tags")
  @Override
  Optional<ExampleEntity> findById(UUID id);
}
