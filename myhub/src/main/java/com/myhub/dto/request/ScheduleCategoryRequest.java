package com.myhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ScheduleCategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 64, message = "分类名称最多 64 个字符")
    private String name;

    @Size(max = 32, message = "颜色值过长")
    private String color;

    private String itemType;
    private Integer sortOrder;
    private Integer status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
