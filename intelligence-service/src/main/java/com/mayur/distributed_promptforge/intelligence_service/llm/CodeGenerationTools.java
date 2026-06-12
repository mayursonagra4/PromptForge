package com.mayur.distributed_promptforge.intelligence_service.llm;

import com.mayur.distributed_promptforge.intelligence_service.client.WorkspaceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class CodeGenerationTools {

    private static final int MAX_FILES_PER_TOOL_CALL = 8;
    private static final int MAX_FILE_CONTENT_CHARS = 250_000;

    private final Long projectId;
    private final WorkspaceClient workspaceClient;

    public record WriteFileRequest(
            @ToolParam(description = "Relative path to write. Examples: index.html, style.css, script.js")
            String path,

            @ToolParam(description = "Complete file content. Do not use placeholders.")
            String content
    ) {
    }

    @Tool(name = "read_files",
            description = "Read files from the current project.")
    public List<String> readFiles(
            @ToolParam(description = "List of relative file paths to read. Examples: [\"index.html\",\"style.css\",\"script.js\"]")
            List<String> paths
    ) {
        List<String> validatedPaths = validateReadFiles(paths);
        List<String> result = new ArrayList<>();

        for (String path : validatedPaths) {
            String cleanPath = normalizePath(path);
            log.info("Tool read_files: projectId={}, path={}", projectId, cleanPath);

            String content = workspaceClient.getFileContent(projectId, cleanPath);
            result.add(String.format(
                    "--- START OF FILE: %s ---%n%s%n--- END OF FILE ---",
                    cleanPath, content
            ));
        }

        return result;
    }

    @Tool(name = "write_files",
            description = "Write one or more complete files.")
    public String writeFiles(
            @ToolParam(description = "List of files to write. Maximum 8 files per call.")
            List<WriteFileRequest> files
    ) {
        List<WriteFileRequest> validatedFiles = validateWriteFiles(files);
        int savedCount = 0;

        for (WriteFileRequest file : validatedFiles) {
            savedCount += saveValidatedFile(file) ? 1 : 0;
        }

        return "ACK: write_files saved " + savedCount + " files.";
    }

    @Tool(name = "write_file",
            description = "Write a single complete file.")
    public String writeFile(
            @ToolParam(description = "Relative path to write. Examples: index.html, style.css, script.js")
            String path,

            @ToolParam(description = "Complete file content. Do not use placeholders.")
            String content
    ) {
        WriteFileRequest file = validateWriteFile(path, content);
        boolean saved = saveValidatedFile(file);
        return saved
                ? "ACK: write_file saved " + normalizePath(file.path()) + "."
                : "ACK: write_file failed to save " + normalizePath(file.path()) + ".";
    }

    private List<String> validateReadFiles(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            throw new IllegalArgumentException("read_files requires at least one path");
        }
        if (paths.size() > MAX_FILES_PER_TOOL_CALL) {
            throw new IllegalArgumentException("read_files supports at most " + MAX_FILES_PER_TOOL_CALL + " paths");
        }
        paths.forEach(this::validatePath);
        log.debug("Tool read_files args: projectId={}, paths={}", projectId, paths);
        return paths;
    }

    private List<WriteFileRequest> validateWriteFiles(List<WriteFileRequest> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("write_files requires at least one file");
        }
        if (files.size() > MAX_FILES_PER_TOOL_CALL) {
            throw new IllegalArgumentException("write_files supports at most " + MAX_FILES_PER_TOOL_CALL + " files");
        }
        files.forEach(file -> validateWriteFile(file.path(), file.content()));
        log.debug("Tool write_files args: projectId={}, fileCount={}", projectId, files.size());
        return files;
    }

    private WriteFileRequest validateWriteFile(String path, String content) {
        validatePath(path);
        if (content == null) {
            throw new IllegalArgumentException("file content is required");
        }
        if (content.length() > MAX_FILE_CONTENT_CHARS) {
            throw new IllegalArgumentException("file content is too large: " + normalizePath(path));
        }
        return new WriteFileRequest(path, content);
    }

    private boolean saveValidatedFile(WriteFileRequest file) {
        String cleanPath = normalizePath(file.path());
        String content = normalizeFileContent(file.content());

        log.info("Tool write file: projectId={}, path={}, contentLength={}",
                projectId, cleanPath, content.length());
        log.debug("Tool write file args: projectId={}, path={}, contentPreview={}",
                projectId, cleanPath, preview(content));

        try {
            workspaceClient.saveFile(projectId, Map.of("path", cleanPath, "content", content));
            return true;
        } catch (Exception ex) {
            log.error("Failed to save file via tool: projectId={}, path={}", projectId, cleanPath, ex);
            return false;
        }
    }

    private void validatePath(String path) {
        String cleanPath = normalizePath(path);
        if (cleanPath.isBlank()) {
            throw new IllegalArgumentException("path is required");
        }
        if (cleanPath.contains("..") || cleanPath.startsWith("~") || cleanPath.contains("\\") || cleanPath.contains(":")) {
            throw new IllegalArgumentException("path must be a safe project-relative path: " + path);
        }
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        String normalized = path.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private String preview(String content) {
        String flattened = content.replace("\r", "\\r").replace("\n", "\\n");
        return flattened.length() <= 500 ? flattened : flattened.substring(0, 500) + "...";
    }

    private String normalizeFileContent(String content) {
        String normalized = content.replace("\u0000", "");
        String trimmed = normalized.trim();
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            int firstLineEnd = trimmed.indexOf('\n');
            if (firstLineEnd > 0) {
                String withoutOpeningFence = trimmed.substring(firstLineEnd + 1);
                int closingFence = withoutOpeningFence.lastIndexOf("```");
                if (closingFence >= 0) {
                    return withoutOpeningFence.substring(0, closingFence).stripTrailing();
                }
            }
        }
        return normalized;
    }
}
