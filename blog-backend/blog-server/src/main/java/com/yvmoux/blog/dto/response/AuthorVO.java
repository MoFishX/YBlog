package com.yvmoux.blog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorVO {
    private Long id;
    private String username;
    private String avatar;
    private String email;
}
