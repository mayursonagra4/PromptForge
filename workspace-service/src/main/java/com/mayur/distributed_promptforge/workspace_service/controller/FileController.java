package com.mayur.distributed_promptforge.workspace_service.controller;

import com.mayur.distributed_promptforge.common_lib.dto.FileTreeDto;
import com.mayur.distributed_promptforge.workspace_service.dto.project.FileContentResponse;
import com.mayur.distributed_promptforge.workspace_service.dto.project.SaveFileRequest;
import com.mayur.distributed_promptforge.workspace_service.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/files")
public class FileController {

    private final ProjectFileService projectFileService;

    @GetMapping
    @PreAuthorize("@security.canViewProject(#p0)")
    public ResponseEntity<FileTreeDto> getFileTree(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectFileService.getFileTree(projectId));
    }

    @GetMapping("/content")
    @PreAuthorize("@security.canViewProject(#p0)")
    public ResponseEntity<FileContentResponse> getFile(
            @PathVariable Long projectId,
            @RequestParam String path) {
        return ResponseEntity.ok(projectFileService.getFileContent(projectId, path));
    }

    @PostMapping("/content")
    @PreAuthorize("@security.canEditProject(#p0)")
    public ResponseEntity<Void> saveFile(
            @PathVariable Long projectId,
            @RequestBody @Valid SaveFileRequest request) {
        projectFileService.saveFile(projectId, request.path(), request.content() != null ? request.content() : "");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download-zip")
    @PreAuthorize("@security.canViewProject(#p0)")
    public ResponseEntity<byte[]> downloadProjectZip(@PathVariable Long projectId) {
        byte[] zipBytes = projectFileService.downloadProjectZip(projectId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=project-" + projectId + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);
    }

}
