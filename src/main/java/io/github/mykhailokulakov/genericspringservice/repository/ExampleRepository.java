package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExampleRepository
    extends JpaRepository<ExampleEntity, UUID>, JpaSpecificationExecutor<ExampleEntity> {

  // Paginated search deliberately does NOT use @EntityGraph on tags: fetch-
  // joining a collection together with Pageable forces Hibernate to apply
  // firstResult/maxResults in memory (HHH000104). The main query stays
  // paginated at SQL level; tags are loaded lazily and batched via
  // @BatchSize on the collection in ExampleEntity.

  @EntityGraph(attributePaths = "tags")
  @Override
  Optional<ExampleEntity> findById(UUID id);
}
