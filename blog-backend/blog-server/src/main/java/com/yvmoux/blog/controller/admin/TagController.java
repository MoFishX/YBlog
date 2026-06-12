package com.yvmoux.blog.controller.admin;

import com.yvmoux.blog.dto.PageResult;
import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.TagRequest;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理-标签")
@RestController("adminTagController")
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "标签列表")
    @GetMapping
    public Result<PageResult<TagVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(tagService.getAllTags(page, pageSize, true));
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<TagVO> create(@Valid @RequestBody TagRequest request) {
        log.info("创建标签, name: {}", request.getName());
        TagVO tag = tagService.createTag(request.getName(), -1L);
        log.info("创建标签成功, tagId: {}", tag.getId());
        return Result.success("创建成功", tag);
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{tagId}")
    public Result<TagVO> update(@PathVariable Long tagId, @Valid @RequestBody TagRequest request) {
        log.info("更新标签, tagId: {}, name: {}", tagId, request.getName());
        TagVO tag = tagService.updateTag(tagId, request.getName());
        log.info("更新标签成功, tagId: {}", tag.getId());
        return Result.success("更新成功", tag);
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{tagId}")
    public Result<Void> delete(@PathVariable Long tagId) {
        log.info("删除标签, tagId: {}", tagId);
        tagService.deleteTag(tagId);
        log.info("删除标签成功, tagId: {}", tagId);
        return Result.success("删除成功", null);
    }
}
