package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.RightEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RightRepository extends JpaRepository<RightEntity, UUID> {}
