package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.LeftEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeftRepository extends JpaRepository<LeftEntity, UUID> {}
