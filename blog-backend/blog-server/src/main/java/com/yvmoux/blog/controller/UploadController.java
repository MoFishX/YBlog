package com.yvmoux.blog.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.enums.ErrorCode;
import com.yvmoux.blog.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Operation(summary = "上传文件")
    @PostMapping
    @SaCheckLogin
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
        Path dir = Paths.get(uploadPath, type, dateStr);
        Files.createDirectories(dir);
        Path targetPath = dir.resolve(newFilename);
        file.transferTo(targetPath.toFile());

        String url = "/uploads/" + type + "/" + dateStr + "/" + newFilename;
        log.info("上传文件成功, url: {}", url);
        return Result.success("上传成功", Map.of(
                "url", url,
                "filename", newFilename,
                "size", file.getSize()
        ));
    }
}
