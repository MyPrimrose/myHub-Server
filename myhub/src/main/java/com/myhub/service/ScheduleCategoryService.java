package com.myhub.service;

import com.myhub.common.exception.BusinessException;
import com.myhub.dto.request.ScheduleCategoryRequest;
import com.myhub.dto.response.ScheduleCategoryResponse;
import com.myhub.entity.ScheduleCategory;
import com.myhub.mapper.ScheduleCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ScheduleCategoryService {
    private final ScheduleCategoryMapper categoryMapper;

    public ScheduleCategoryService(ScheduleCategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<ScheduleCategoryResponse> list(String itemType, boolean includeDisabled) {
        return categoryMapper.selectList(blankToNull(itemType), includeDisabled).stream()
                .map(this::toResponse)
                .toList();
    }

    public ScheduleCategoryResponse getById(Long id) {
        return toResponse(require(id));
    }

    @Transactional
    public ScheduleCategoryResponse create(ScheduleCategoryRequest request) {
        ScheduleCategory category = fromRequest(request, null);
        categoryMapper.insert(category);
        return getById(category.getId());
    }

    @Transactional
    public ScheduleCategoryResponse update(Long id, ScheduleCategoryRequest request) {
        ScheduleCategory existing = require(id);
        ScheduleCategory category = fromRequest(request, existing);
        category.setId(id);
        categoryMapper.update(category);
        return getById(id);
    }

    @Transactional
    public void disable(Long id) {
        require(id);
        categoryMapper.disable(id);
    }

    private ScheduleCategory fromRequest(ScheduleCategoryRequest request, ScheduleCategory existing) {
        ScheduleCategory category = new ScheduleCategory();
        category.setName(request.getName().trim());
        category.setColor(StringUtils.hasText(request.getColor())
                ? request.getColor().trim()
                : (existing != null && existing.getColor() != null ? existing.getColor() : "#8b6df0"));
        String itemType = StringUtils.hasText(request.getItemType())
                ? request.getItemType().trim().toUpperCase()
                : (existing != null ? existing.getItemType() : "ALL");
        if (!List.of("PLAN", "TASK", "ALL").contains(itemType)) {
            throw new BusinessException(400, "分类类型仅支持 PLAN、TASK 或 ALL");
        }
        category.setItemType(itemType);
        category.setSortOrder(request.getSortOrder() != null
                ? request.getSortOrder()
                : (existing != null && existing.getSortOrder() != null ? existing.getSortOrder() : 0));
        category.setStatus(request.getStatus() != null
                ? request.getStatus()
                : (existing != null && existing.getStatus() != null ? existing.getStatus() : 1));
        return category;
    }

    private ScheduleCategory require(Long id) {
        if (id == null) {
            throw new BusinessException(400, "分类 ID 不能为空");
        }
        ScheduleCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return category;
    }

    private ScheduleCategoryResponse toResponse(ScheduleCategory category) {
        ScheduleCategoryResponse response = new ScheduleCategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setColor(category.getColor());
        response.setItemType(category.getItemType());
        response.setSortOrder(category.getSortOrder());
        response.setStatus(category.getStatus());
        response.setCreateTime(category.getCreateTime());
        response.setUpdateTime(category.getUpdateTime());
        return response;
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
