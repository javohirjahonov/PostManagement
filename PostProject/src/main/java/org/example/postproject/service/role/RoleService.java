package org.example.postproject.service.role;

import lombok.RequiredArgsConstructor;
import org.example.postproject.api.dtos.RoleAssignDto;
import org.example.postproject.api.dtos.RoleDto;
import org.example.postproject.api.dtos.response.StandardResponse;
import org.example.postproject.api.dtos.response.Status;
import org.example.postproject.entities.role.PermissionEntity;
import org.example.postproject.entities.role.RoleEntity;
import org.example.postproject.entities.user.UserEntity;
import org.example.postproject.exception.DataNotFoundException;
import org.example.postproject.exception.UniqueObjectException;
import org.example.postproject.exception.UserBadRequestException;
import org.example.postproject.repository.PermissionRepository;
import org.example.postproject.repository.RoleRepository;
import org.example.postproject.repository.UserRepository;
import org.example.postproject.service.jwt.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;


    public StandardResponse<RoleEntity> save(RoleDto roleDto) {
        RoleEntity roleEntityByName = roleRepository.findRoleEntitiesByName(roleDto.getName());
        if (roleEntityByName != null) throw new UniqueObjectException("Role already exists");
        List<String> permissions = roleDto.getPermissions();
        List<PermissionEntity> rolePermission = new ArrayList<>();
        for (String permission : permissions) {
            PermissionEntity permissionEntitiesByPermission = permissionRepository.findPermissionEntitiesByPermission(permission);
            if (permissionEntitiesByPermission == null) {
                permissionEntitiesByPermission = PermissionEntity.builder().permission(permission).build();
                rolePermission.add(permissionRepository.save(permissionEntitiesByPermission));
            }else {
                rolePermission.add(permissionEntitiesByPermission);
            }
        }
        RoleEntity roleEntity = RoleEntity.builder().name(roleDto.getName()).permissions(rolePermission).build();
        roleEntity = roleRepository.save(roleEntity);
        return StandardResponse.<RoleEntity>builder().status(Status.SUCCESS).message("Role successfully created").data(roleEntity).build();
    }

    public StandardResponse<RoleEntity> getRole(String name) {
        RoleEntity roleEntity = roleRepository.findRoleEntityByName(name).orElseThrow(() -> new DataNotFoundException("Role not found"));
        return StandardResponse.<RoleEntity>builder().status(Status.SUCCESS).message("Role successfully sent").data(roleEntity).build();
    }

    public StandardResponse<RoleEntity> update(RoleDto roleDto) {
        RoleEntity roleEntityByName = roleRepository.findRoleEntityByName(roleDto.getName()).orElseThrow(() -> new DataNotFoundException("Role not found"));

        if(roleDto.getPermissions() != null) {
            List<PermissionEntity> updatedPermissions = new ArrayList<>();
            for (String roleDtoPermission : roleDto.getPermissions()) {
                PermissionEntity permission = permissionRepository.findPermissionEntitiesByPermission(roleDtoPermission);
                if (permission != null) {
                    updatedPermissions.add(permission);
                } else {
                    PermissionEntity build = PermissionEntity.builder().permission(roleDtoPermission).build();
                    updatedPermissions.add(permissionRepository.save(build));
                }
            }
            roleEntityByName.setPermissions(updatedPermissions);
        }
        roleEntityByName.setUpdatedDate(LocalDateTime.now());
        return StandardResponse.<RoleEntity>builder().status(Status.SUCCESS)
                .message("Permissions successfully added to the role")
                .data(roleRepository.save(roleEntityByName))
                .build();
    }

    public StandardResponse<String> assignRoleToUser(RoleAssignDto roleAssignDto, Principal principal) {
        if(Objects.equals(roleAssignDto.getName(), "OWNER")) throw new AccessDeniedException("Unacceptable role name");
        RoleEntity roleEntity = roleRepository.findRoleEntityByName(roleAssignDto.getName())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        UserEntity user = userRepository.findByEmail(roleAssignDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        UserEntity userEntity = userRepository.findByEmail(principal.getName()).orElseThrow();

        List<RoleEntity> roles = user.getRoles();
        for (RoleEntity role : roles) {
            if(role.equals(roleEntity)) throw new UserBadRequestException("User already has "+role.getName()+" role");
        }
        List<String> permissions = roleAssignDto.getPermissions();
        List<PermissionEntity> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            for (PermissionEntity roleEntityPermission : roleEntity.getPermissions()) {
                if(permission.equals(roleEntityPermission.getPermission())){
                    permissionList.add(roleEntityPermission);
                }
            }
        }
        roles.add(roleEntity);
        user.setRoles(roles);
        user.setPermissions(permissionList);
        userRepository.save(user);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Role successfully assigned to " + user.getEmail()).build();
    }

    public StandardResponse<String> addPermissionsToUser(RoleAssignDto roleAssignDto) {
        RoleEntity roleEntity = roleRepository.findRoleEntityByName(roleAssignDto.getName())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        UserEntity user = userRepository.findByEmail(roleAssignDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        List<PermissionEntity> permissionList = user.getPermissions();

        List<RoleEntity> roles = user.getRoles();
        for (RoleEntity role : roles) {
            if(role.equals(roleEntity)){
                List<String> permissions = roleAssignDto.getPermissions();
                for (String permission : permissions) {
                    for (PermissionEntity roleEntityPermission : roleEntity.getPermissions()) {
                        if(permission.equals(roleEntityPermission.getPermission())){
                            permissionList.add(roleEntityPermission);
                        }
                    }
                }
                break;
            }
        }

        user.setPermissions(permissionList);
        userRepository.save(user);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Permissions successfully added to "+user.getEmail()).build();
    }

}
