package org.example.postproject.repository;

import org.example.postproject.entities.role.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findRoleEntityByName(String name);
    RoleEntity findRoleEntitiesByName(String name);
}
