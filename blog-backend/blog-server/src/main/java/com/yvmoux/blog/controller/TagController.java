package com.yvmoux.blog.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return Result.success(tagService.getAllTags());
    }

    @Operation(summary = "创建标签")
    @PostMapping
    @SaCheckRole("ADMIN")
    public Result<TagVO> create(@Valid @RequestBody TagRequest request) {
        TagVO tag = tagService.createTag(request.getName());
        return Result.success("创建成功", tag);
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{tagId}")
    @SaCheckRole("ADMIN")
    public Result<TagVO> update(@PathVariable Long tagId, @Valid @RequestBody TagRequest request) {
        TagVO tag = tagService.updateTag(tagId, request.getName());
        return Result.success("更新成功", tag);
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{tagId}")
    @SaCheckRole("ADMIN")
    public Result<Void> delete(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return Result.success("删除成功", null);
    }
}
