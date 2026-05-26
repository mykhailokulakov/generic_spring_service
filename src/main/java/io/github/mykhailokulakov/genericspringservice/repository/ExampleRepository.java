package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ExampleEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;

public interface ExampleRepository extends FilterableRepository<ExampleEntity> {

  @EntityGraph(attributePaths = "tags")
  @Override
  Optional<ExampleEntity> findById(UUID id);
}
