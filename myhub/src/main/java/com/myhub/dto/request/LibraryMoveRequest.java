package com.myhub.dto.request;

public class LibraryMoveRequest {
    /** Null means move to root / uncategorized. */
    private Long targetFolderId;

    public Long getTargetFolderId() { return targetFolderId; }
    public void setTargetFolderId(Long targetFolderId) { this.targetFolderId = targetFolderId; }
}
