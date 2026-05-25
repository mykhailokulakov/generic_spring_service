package io.github.mykhailokulakov.testentities.repository;

import io.github.mykhailokulakov.testentities.RightEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RightEntityRepository extends JpaRepository<RightEntity, UUID> {}
