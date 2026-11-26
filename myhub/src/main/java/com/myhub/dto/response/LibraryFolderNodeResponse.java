package com.myhub.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LibraryFolderNodeResponse {
    private Long id;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private int fileCount;
    private LocalDateTime createTime;
    private List<LibraryFolderNodeResponse> children = new ArrayList<>();
    private List<LibraryFileResponse> files = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public List<LibraryFolderNodeResponse> getChildren() { return children; }
    public void setChildren(List<LibraryFolderNodeResponse> children) { this.children = children; }
    public List<LibraryFileResponse> getFiles() { return files; }
    public void setFiles(List<LibraryFileResponse> files) { this.files = files; }
}
