package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ParentEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentRepository extends JpaRepository<ParentEntity, UUID> {}
