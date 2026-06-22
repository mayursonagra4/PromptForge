package com.mayur.distributed_promptforge.workspace_service.consumer;

import com.mayur.distributed_promptforge.common_lib.enums.ProjectRole;
import com.mayur.distributed_promptforge.common_lib.event.UserDeletionRequestEvent;
import com.mayur.distributed_promptforge.common_lib.event.UserDeletionResponseEvent;
import com.mayur.distributed_promptforge.workspace_service.entity.ProjectMember;
import com.mayur.distributed_promptforge.workspace_service.repository.ProjectMemberRepository;
import com.mayur.distributed_promptforge.workspace_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDeletionConsumer {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectService projectService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "user-deletion-request-event", groupId = "workspace-group")
    public void consumeUserDeletionRequest(UserDeletionRequestEvent event) {
        Long userId = event.userId();
        log.info("Received UserDeletionRequestEvent for userId={}", userId);

        try {
            List<ProjectMember> memberships = projectMemberRepository.findAllByUserId(userId);
            log.info("Found {} memberships to clean up for userId={}", memberships.size(), userId);

            for (ProjectMember pm : memberships) {
                Long projectId = pm.getId().getProjectId();
                if (ProjectRole.OWNER.equals(pm.getProjectRole())) {
                    log.info("Deleting project {} owned by user {}", projectId, userId);
                    projectService.deleteProjectInternal(projectId);
                } else {
                    log.info("Removing user {} collaborator membership from project {}", userId, projectId);
                    projectMemberRepository.delete(pm);
                }
            }

            // Send successful response back to Kafka
            kafkaTemplate.send("user-deletion-response-event", String.valueOf(userId),
                    new UserDeletionResponseEvent(userId, true, null));
            log.info("Successfully cleaned up workspaces and published success response for userId={}", userId);

        } catch (Exception e) {
            log.error("Failed to clean up workspaces for userId={}: {}", userId, e.getMessage(), e);
            // Send failure response back to Kafka
            kafkaTemplate.send("user-deletion-response-event", String.valueOf(userId),
                    new UserDeletionResponseEvent(userId, false, e.getMessage()));
        }
    }
}
