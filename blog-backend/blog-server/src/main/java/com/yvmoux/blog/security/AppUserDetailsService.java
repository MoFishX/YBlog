package com.yvmoux.blog.security;

import com.yvmoux.blog.entity.User;
import com.yvmoux.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                        .eq("username", username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new AppUserDetails(user);
    }
}
