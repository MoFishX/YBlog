package com.yvmoux.blog.converter;

import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.mapper.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class UserConverter {

    private final UserMapper userMapper;

    public UserVO toUserVO(User user) {
        int i = userMapper.countArticlesByUserId(user.getId());
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .articleCount(i)
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public List<UserVO> toUserVOList(List<User> users) {
        return users.stream().map(this::toUserVO).toList();
    }
}
