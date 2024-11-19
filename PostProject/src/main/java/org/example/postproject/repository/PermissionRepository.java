package org.example.postproject.repository;

import org.example.postproject.entities.role.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {
    List<PermissionEntity> findPermissionEntitiesByPermissionIn(List<String> permissions);
    PermissionEntity findPermissionEntitiesByPermission(String permission);
}