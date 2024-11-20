package org.example.postproject.repository;

import org.example.postproject.entities.role.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
    PermissionEntity findPermissionEntitiesByPermission(String permission);
}