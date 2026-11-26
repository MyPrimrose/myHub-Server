package com.myhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class ScheduleItemRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最多 200 个字符")
    private String title;

    @NotBlank(message = "事项类型不能为空")
    private String itemType;

    @Size(max = 5000, message = "描述过长")
    private String description;

    private String status;
    private String priority;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime dueAt;
    private Boolean allDay;
    private Long categoryId;
    private Long relatedPlanId;

    @Size(max = 255, message = "地点最多 255 个字符")
    private String location;

    private Integer sortOrder;

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
    public Long getRelatedPlanId() { return relatedPlanId; }
    public void setRelatedPlanId(Long relatedPlanId) { this.relatedPlanId = relatedPlanId; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
