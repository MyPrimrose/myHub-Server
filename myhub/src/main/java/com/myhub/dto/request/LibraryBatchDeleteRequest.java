package com.myhub.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class LibraryBatchDeleteRequest {
    @NotEmpty(message = "请选择要删除的文件")
    private List<Long> ids;

    public List<Long> getIds() { return ids; }
    public void setIds(List<Long> ids) { this.ids = ids; }
}
