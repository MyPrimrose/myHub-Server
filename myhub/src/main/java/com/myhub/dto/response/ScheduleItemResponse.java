package com.myhub.dto.response;

import java.time.LocalDateTime;

public class ScheduleItemResponse {
    private Long id;
    private String title;
    private String itemType;
    private String description;
    private String status;
    private String priority;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime dueAt;
    private Boolean allDay;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private Long relatedPlanId;
    private String relatedPlanTitle;
    private String location;
    private Integer sortOrder;
    private Integer durationDays;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public LocalDateTime getDueAt() { return dueAt; }
    public void setDueAt(LocalDateTime dueAt) { this.dueAt = dueAt; }
    public Boolean getAllDay() { return allDay; }
    public void setAllDay(Boolean allDay) { this.allDay = allDay; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getCategoryColor() { return categoryColor; }
    public void setCategoryColor(String categoryColor) { this.categoryColor = categoryColor; }
    public Long getRelatedPlanId() { return relatedPlanId; }
    public void setRelatedPlanId(Long relatedPlanId) { this.relatedPlanId = relatedPlanId; }
    public String getRelatedPlanTitle() { return relatedPlanTitle; }
    public void setRelatedPlanTitle(String relatedPlanTitle) { this.relatedPlanTitle = relatedPlanTitle; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
