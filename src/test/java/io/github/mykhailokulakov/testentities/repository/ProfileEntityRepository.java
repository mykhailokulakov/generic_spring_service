package io.github.mykhailokulakov.testentities.repository;

import io.github.mykhailokulakov.testentities.ProfileEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileEntityRepository extends JpaRepository<ProfileEntity, UUID> {}
