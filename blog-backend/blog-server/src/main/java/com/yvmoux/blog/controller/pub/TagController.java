package com.yvmoux.blog.controller.pub;

import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.TagVO;
import com.yvmoux.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "公共-标签")
@RestController("pubTagController")
@RequestMapping("/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;

    @Operation(summary = "标签列表")
    @GetMapping
    public Result<List<TagVO>> list() {
        log.info("获取标签列表");
        Result<List<TagVO>> result = Result.success(tagService.getAllTags());
        log.info("获取标签列表成功, count: {}", result.getData().size());
        return result;
    }
}
