package com.yvmoux.blog.converter;

import com.yvmoux.blog.dto.response.UserVO;
import com.yvmoux.blog.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserConverter {
    default UserVO toUserVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .role(user.getRole())
                .articleCount(user.getArticleCount())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }

    default List<UserVO> toUserVOList(List<User> users) {
        return users.stream().map(this::toUserVO).toList();
    }
}
