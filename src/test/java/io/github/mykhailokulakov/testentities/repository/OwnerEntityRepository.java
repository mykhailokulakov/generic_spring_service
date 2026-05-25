package io.github.mykhailokulakov.testentities.repository;

import io.github.mykhailokulakov.testentities.OwnerEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerEntityRepository extends JpaRepository<OwnerEntity, UUID> {}
