package com.mayur.distributed_promptforge.workspace_service.security;

import com.mayur.distributed_promptforge.common_lib.enums.ProjectPermission;
import com.mayur.distributed_promptforge.common_lib.security.AuthUtil;
import com.mayur.distributed_promptforge.workspace_service.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("security")
@RequiredArgsConstructor
@Slf4j
public class SecurityExpressions {

    private final ProjectMemberRepository projectMemberRepository;
    private final AuthUtil authUtil;

    public boolean hasPermission(Long projectId, ProjectPermission projectPermission) {
        Long userId = authUtil.getCurrentUserId();
        return hasPermission(projectId, userId, projectPermission);
    }

    public boolean hasPermission(Long projectId, Long userId, ProjectPermission projectPermission) {
        log.debug("=== SECURITY PERMISSION CHECK ===");
        log.debug("projectId: {}", projectId);
        log.debug("userId: {}", userId);
        log.debug("permission: {}", projectPermission);
        boolean result = projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId).
                map(role -> {
                    log.debug("Found user role: {}", role);
                    log.debug("Permissions: {}", role.getPermissions());
                    return role.getPermissions().contains(projectPermission);
                })
                .orElse(false);
        log.debug("result: {}", result);
        log.debug("=================================");
        return result;
    }

    public boolean canViewProject(Long projectId) {
        return hasPermission(projectId, ProjectPermission.VIEW);
    }

    public boolean canEditProject(Long projectId) {
        return hasPermission(projectId, ProjectPermission.EDIT);
    }

    public boolean canDeleteProject(Long projectId) {
        return hasPermission(projectId, ProjectPermission.DELETE);
    }

    public boolean canViewMembers(Long projectId) {
        return hasPermission(projectId, ProjectPermission.VIEW_MEMBERS);
    }

    public boolean canManageMembers(Long projectId) {
        return hasPermission(projectId, ProjectPermission.MANAGE_MEMBERS);
    }
}
