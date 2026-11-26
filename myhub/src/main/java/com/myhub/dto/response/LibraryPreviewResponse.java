package com.myhub.dto.response;

public class LibraryPreviewResponse {
    private Long id;
    private String name;
    private Long fileSize;
    private String content;
    private String isolationMode;

    public LibraryPreviewResponse() {}

    public LibraryPreviewResponse(Long id, String name, Long fileSize, String content, String isolationMode) {
        this.id = id;
        this.name = name;
        this.fileSize = fileSize;
        this.content = content;
        this.isolationMode = isolationMode;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getIsolationMode() { return isolationMode; }
    public void setIsolationMode(String isolationMode) { this.isolationMode = isolationMode; }
}
