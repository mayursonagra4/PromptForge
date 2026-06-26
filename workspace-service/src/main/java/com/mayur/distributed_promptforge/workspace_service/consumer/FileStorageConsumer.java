package com.mayur.distributed_promptforge.workspace_service.consumer;

import com.mayur.distributed_promptforge.common_lib.event.FileStoreRequestEvent;
import com.mayur.distributed_promptforge.common_lib.event.FileStoreResponseEvent;
import com.mayur.distributed_promptforge.workspace_service.entity.ProcessedEvent;
import com.mayur.distributed_promptforge.workspace_service.repository.ProcessedEventRepository;
import com.mayur.distributed_promptforge.workspace_service.service.ProjectFileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageConsumer {

    private final ProjectFileService projectFileService;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 2000, multiplier = 2.0),
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "file-storage-request-event", groupId = "workspace-group")
    public void consumeFileEvent(FileStoreRequestEvent requestEvent) {

        if (processedEventRepository.existsById(requestEvent.sagaId())) {
            log.info("Duplicate Saga detected: {}. Resending previous ACK.", requestEvent.sagaId());
            sendResponse(requestEvent, true, null);
            return;
        }

        log.info("Saving file: {}", requestEvent.filePath());
        projectFileService.saveFile(requestEvent.projectId(), requestEvent.filePath(), requestEvent.content());

        processedEventRepository.save(new ProcessedEvent(
                requestEvent.sagaId(), LocalDateTime.now()
        ));

        sendResponse(requestEvent, true, null);
    }

    @DltHandler
    public void handleDlt(FileStoreRequestEvent requestEvent, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        log.error("Failed to store file after all retries in DLT (topic={}): path={}, sagaId={}",
                topic, requestEvent.filePath(), requestEvent.sagaId());
        sendResponse(requestEvent, false, "Storage failed after maximum retries");
    }

    private void sendResponse(FileStoreRequestEvent req, boolean success, String error) {
        FileStoreResponseEvent response = FileStoreResponseEvent.builder()
                .sagaId(req.sagaId())
                .projectId(req.projectId())
                .success(success)
                .errorMessage(error)
                .build();
        kafkaTemplate.send("file-store-responses", response);
    }
}
