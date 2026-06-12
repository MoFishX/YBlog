package com.yvmoux.blog.controller;

import com.yvmoux.blog.config.StorageConfig;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.exception.BusinessException;
import com.yvmoux.blog.utils.QiNiuOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final QiNiuOssUtil ossUtil;
    private final StorageConfig storageConfig;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 本地上传目录，绝对路径
    private Path resolvedUploadPath;

    @PostConstruct
    private void init() {
        resolvedUploadPath = Paths.get(storageConfig.getLocal().getPath()).toAbsolutePath();
        log.info("上传目录: {}", resolvedUploadPath);
    }

    @Operation(summary = "上传文件")
    @PostMapping
    @PreAuthorize("isAuthenticated() and @authChecker.isActive()")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam("type") String type) throws IOException {
        log.info("上传文件, type: {}, originalFilename: {}, size: {}", type, file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_INVALID);
        }

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String objectName = type + "/" + dateStr + "/" + newFilename;

        String url;
        if ("oss".equalsIgnoreCase(storageConfig.getType())) {
            url = ossUtil.upload(file.getBytes(), objectName);
            log.info("上传文件到OSS成功, url: {}", url);
        } else {
            Path dir = resolvedUploadPath.resolve(type).resolve(dateStr);
            Files.createDirectories(dir);
            Path targetPath = dir.resolve(newFilename);
            file.transferTo(targetPath.toAbsolutePath().toFile());
            url = "/uploads/" + objectName;
            log.info("上传文件到本地成功, url: {}", url);
        }

        return Result.success("上传成功", Map.of(
                "url", url,
                "filename", newFilename,
                "size", file.getSize()
        ));
    }
}
