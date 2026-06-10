package com.mayur.distributed_promptforge.workspace_service.repository;

import com.mayur.distributed_promptforge.workspace_service.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
