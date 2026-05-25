package io.github.mykhailokulakov.genericspringservice.repository;

import io.github.mykhailokulakov.genericspringservice.domain.entity.ChildEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildRepository extends JpaRepository<ChildEntity, UUID> {}
