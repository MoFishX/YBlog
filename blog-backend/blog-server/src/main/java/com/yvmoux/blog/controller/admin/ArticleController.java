package com.yvmoux.blog.controller.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "文章")
@RestController("adminArticleController")
@RequestMapping("/admin/articles")
@RequiredArgsConstructor
public class ArticleController {
}
