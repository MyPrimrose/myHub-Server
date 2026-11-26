package com.myhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LibraryFileUpdateRequest {
    @NotBlank(message = "文件名称不能为空")
    @Size(max = 200, message = "文件名称最多 200 个字符")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
