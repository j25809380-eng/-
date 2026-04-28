package com.fitnote.backend.common;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestPart("file") MultipartFile file,
                                                   @RequestParam(defaultValue = "common") String category,
                                                   @RequestParam(required = false) String fileName) {
        String originalName = (fileName == null || fileName.isBlank()) ? file.getOriginalFilename() : fileName;
        String extension = originalName != null && originalName.contains(".")
            ? originalName.substring(originalName.lastIndexOf('.'))
            : ".png";
        String safeCategory = category.replaceAll("[^a-zA-Z0-9_-]", "");
        String safeName = safeCategory + "-" + System.currentTimeMillis() + extension;

        try {
            Path uploadDir = Paths.get("uploads", safeCategory);
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(safeName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            throw new RuntimeException("文件上传失败", ex);
        }

        return ApiResponse.ok(Map.of(
            "fileName", safeName,
            "category", safeCategory,
            "url", "/uploads/" + safeCategory + "/" + safeName
        ));
    }
}
