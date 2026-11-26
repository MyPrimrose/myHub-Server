package com.myhub.common.constant;

public enum SchedulePriority {
    LOW,
    MEDIUM,
    HIGH;

    public static SchedulePriority from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("优先级不能为空");
        }
        return SchedulePriority.valueOf(value.trim().toUpperCase());
    }
}
