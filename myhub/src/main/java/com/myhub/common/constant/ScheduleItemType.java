package com.myhub.common.constant;

public enum ScheduleItemType {
    PLAN,
    TASK;

    public static ScheduleItemType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("事项类型不能为空");
        }
        return ScheduleItemType.valueOf(value.trim().toUpperCase());
    }
}
