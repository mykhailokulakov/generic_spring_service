package io.github.mykhailokulakov.testentities.repository;

import io.github.mykhailokulakov.testentities.ChildEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildEntityRepository extends JpaRepository<ChildEntity, UUID> {}
