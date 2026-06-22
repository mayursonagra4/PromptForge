package com.mayur.distributed_promptforge.account_service.consumer;

import com.mayur.distributed_promptforge.account_service.repository.UserRepository;
import com.mayur.distributed_promptforge.common_lib.event.UserDeletionResponseEvent;
import com.mayur.distributed_promptforge.account_service.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDeletionResponseConsumer {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @KafkaListener(topics = "user-deletion-response-event", groupId = "account-group")
    @Transactional
    public void consumeUserDeletionResponse(UserDeletionResponseEvent event) {
        log.info("Received UserDeletionResponseEvent for userId={}. Success: {}, Error: {}",
                event.userId(), event.success(), event.errorMessage());

        if (event.success()) {
            try {
                if (userRepository.existsById(event.userId())) {
                    // First delete all subscriptions referencing the user to satisfy the foreign key constraint
                    subscriptionRepository.deleteByUserId(event.userId());
                    log.info("Deleted subscriptions for user {}", event.userId());

                    userRepository.deleteById(event.userId());
                    log.info("Permanently deleted user {} from database after successful workspace cleanup.", event.userId());
                } else {
                    log.warn("Received deletion response for non-existing user {}.", event.userId());
                }
            } catch (Exception e) {
                log.error("Failed to permanently delete user {} from database: {}", event.userId(), e.getMessage(), e);
            }
        } else {
            log.error("Workspace cleanup failed for user {}: {}", event.userId(), event.errorMessage());
        }
    }
}
