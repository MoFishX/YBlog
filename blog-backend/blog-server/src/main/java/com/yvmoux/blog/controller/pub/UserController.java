package com.yvmoux.blog.controller.pub;

import com.yvmoux.blog.dto.Result;
import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.security.SecurityUtils;
import com.yvmoux.blog.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公共-用户")
@RestController("pubUserController")
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping("/info")
    @Operation(summary = "用户信息")
    public Result<UserVO> info() {
        log.info("获取用户信息");
        Long currentUserId = securityUtils.getCurrentUserId();
        UserVO userVO = userService.getUserById(currentUserId);
        log.info("获取用户信息成功, userId: {}", userVO.getId());
        return Result.success(userVO);
    }
}
