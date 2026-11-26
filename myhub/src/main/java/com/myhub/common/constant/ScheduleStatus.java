package com.myhub.common.constant;

public enum ScheduleStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public static ScheduleStatus from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("状态不能为空");
        }
        return ScheduleStatus.valueOf(value.trim().toUpperCase());
    }
}
