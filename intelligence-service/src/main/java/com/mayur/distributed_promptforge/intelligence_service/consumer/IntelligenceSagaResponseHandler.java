package com.mayur.distributed_promptforge.intelligence_service.consumer;

import com.mayur.distributed_promptforge.common_lib.enums.ChatEventStatus;
import com.mayur.distributed_promptforge.common_lib.event.FileStoreResponseEvent;
import com.mayur.distributed_promptforge.intelligence_service.repository.ChatEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IntelligenceSagaResponseHandler {

    private final ChatEventRepository chatEventRepository;

    @Transactional
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 1000, multiplier = 2.0),
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "file-store-responses", groupId = "intelligence-group")
    public void handleSagaResponse(FileStoreResponseEvent response) {

        chatEventRepository.findBySagaId(response.sagaId()).ifPresent(event -> {

            if (!ChatEventStatus.PENDING.equals(event.getStatus())) {
                log.info("Response for Saga {} already handled. Skipping.", response.sagaId());
                return;
            }

            if (response.success()) {
                event.setStatus(ChatEventStatus.CONFIRMED);
                log.info("Saga {} CONFIRMED", response.sagaId());
            } else {
                log.warn("Saga {} FAILED. Setting status to FAILED.", response.sagaId());
                event.setStatus(ChatEventStatus.FAILED);
            }
        });
    }

    @DltHandler
    public void handleDlt(FileStoreResponseEvent response, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Failed to process Saga response event after all retries in DLT (topic={}): sagaId={}, success={}",
                topic, response.sagaId(), response.success());
    }
}
