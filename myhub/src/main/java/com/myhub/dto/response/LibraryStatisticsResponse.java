package com.myhub.dto.response;

public class LibraryStatisticsResponse {
    private int folderCount;
    private int fileCount;
    private long usedBytes;
    private long maxBytes;
    private double usedPercent;

    public LibraryStatisticsResponse() {}

    public LibraryStatisticsResponse(int folderCount, int fileCount, long usedBytes, long maxBytes) {
        this.folderCount = folderCount;
        this.fileCount = fileCount;
        this.usedBytes = usedBytes;
        this.maxBytes = maxBytes;
        this.usedPercent = maxBytes <= 0 ? 0 : Math.min(100.0, usedBytes * 100.0 / maxBytes);
    }

    public int getFolderCount() { return folderCount; }
    public void setFolderCount(int folderCount) { this.folderCount = folderCount; }
    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }
    public long getUsedBytes() { return usedBytes; }
    public void setUsedBytes(long usedBytes) { this.usedBytes = usedBytes; }
    public long getMaxBytes() { return maxBytes; }
    public void setMaxBytes(long maxBytes) { this.maxBytes = maxBytes; }
    public double getUsedPercent() { return usedPercent; }
    public void setUsedPercent(double usedPercent) { this.usedPercent = usedPercent; }
}
