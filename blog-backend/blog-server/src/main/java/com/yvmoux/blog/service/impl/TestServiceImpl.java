package com.yvmoux.blog.service.impl;

import com.yvmoux.blog.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final PasswordEncoder passwordEncoder;
}
