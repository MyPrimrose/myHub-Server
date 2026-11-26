package com.myhub.dto.response;

public class ScheduleStatisticsResponse {
    private int totalCount;
    private int highPriorityCount;
    private int inProgressCount;
    private int completedCount;
    private int remainingDays;

    public ScheduleStatisticsResponse() {}

    public ScheduleStatisticsResponse(int totalCount, int highPriorityCount, int inProgressCount,
                                      int completedCount, int remainingDays) {
        this.totalCount = totalCount;
        this.highPriorityCount = highPriorityCount;
        this.inProgressCount = inProgressCount;
        this.completedCount = completedCount;
        this.remainingDays = remainingDays;
    }

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getHighPriorityCount() { return highPriorityCount; }
    public void setHighPriorityCount(int highPriorityCount) { this.highPriorityCount = highPriorityCount; }
    public int getInProgressCount() { return inProgressCount; }
    public void setInProgressCount(int inProgressCount) { this.inProgressCount = inProgressCount; }
    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
    public int getRemainingDays() { return remainingDays; }
    public void setRemainingDays(int remainingDays) { this.remainingDays = remainingDays; }
}
