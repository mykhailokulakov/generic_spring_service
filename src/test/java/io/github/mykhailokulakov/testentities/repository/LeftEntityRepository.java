package io.github.mykhailokulakov.testentities.repository;

import io.github.mykhailokulakov.testentities.LeftEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeftEntityRepository extends JpaRepository<LeftEntity, UUID> {}
