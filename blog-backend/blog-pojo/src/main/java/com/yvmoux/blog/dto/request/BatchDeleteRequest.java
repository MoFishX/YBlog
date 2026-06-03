package com.yvmoux.blog.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteRequest {
    @NotNull
    @Size(min = 1)
    private List<Long> ids;
}
