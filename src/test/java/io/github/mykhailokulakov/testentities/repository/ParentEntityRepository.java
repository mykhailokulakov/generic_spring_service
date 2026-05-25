package io.github.mykhailokulakov.testentities.repository;

import io.github.mykhailokulakov.testentities.ParentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentEntityRepository extends JpaRepository<ParentEntity, UUID> {}
