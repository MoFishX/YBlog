package com.yvmoux.blog.controller;

import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "标签")
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Data
    static class TagRequest {
        @NotBlank @Size(min = 1, max = 30)
        private String name;
    }

    @Operation(summary = "标签列表")
    @GetMapping
    public Result<List<TagVO>> list() {
        log.info("获取标签列表");
        Result<List<TagVO>> result = Result.success(tagService.getAllTags());
        log.info("获取标签列表成功, count: {}", result.getData().size());
        return result;
    }

    @Operation(summary = "创建标签")
    @PostMapping
//    @SaCheckRole("ADMIN")
    public Result<TagVO> create(@Valid @RequestBody TagRequest request) {
        log.info("创建标签, name: {}", request.getName());
        TagVO tag = tagService.createTag(request.getName());
        log.info("创建标签成功, tagId: {}", tag.getId());
        return Result.success("创建成功", tag);
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{tagId}")
//    @SaCheckRole("ADMIN")
    public Result<TagVO> update(@PathVariable Long tagId, @Valid @RequestBody TagRequest request) {
        log.info("更新标签, tagId: {}, name: {}", tagId, request.getName());
        TagVO tag = tagService.updateTag(tagId, request.getName());
        log.info("更新标签成功, tagId: {}", tag.getId());
        return Result.success("更新成功", tag);
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{tagId}")
//    @SaCheckRole("ADMIN")
    public Result<Void> delete(@PathVariable Long tagId) {
        log.info("删除标签, tagId: {}", tagId);
        tagService.deleteTag(tagId);
        log.info("删除标签成功, tagId: {}", tagId);
        return Result.success("删除成功", null);
    }
}
