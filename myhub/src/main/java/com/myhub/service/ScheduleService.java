package com.myhub.service;

import com.myhub.common.constant.ScheduleItemType;
import com.myhub.common.constant.SchedulePriority;
import com.myhub.common.constant.ScheduleStatus;
import com.myhub.common.exception.BusinessException;
import com.myhub.dto.request.ScheduleItemRequest;
import com.myhub.dto.response.ScheduleItemResponse;
import com.myhub.dto.response.ScheduleStatisticsResponse;
import com.myhub.entity.ScheduleCategory;
import com.myhub.entity.ScheduleItem;
import com.myhub.mapper.ScheduleCategoryMapper;
import com.myhub.mapper.ScheduleItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ScheduleService {
    public static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final ScheduleItemMapper itemMapper;
    private final ScheduleCategoryMapper categoryMapper;

    public ScheduleService(ScheduleItemMapper itemMapper, ScheduleCategoryMapper categoryMapper) {
        this.itemMapper = itemMapper;
        this.categoryMapper = categoryMapper;
    }

    public List<ScheduleItemResponse> listItems(LocalDate startDate, LocalDate endDate, String itemType,
                                                String status, String priority, Long categoryId, String keyword) {
        if (startDate == null || endDate == null) {
            throw new BusinessException(400, "开始日期和结束日期不能为空");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(400, "结束日期不能早于开始日期");
        }
        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = endDate.atTime(LocalTime.MAX).withNano(0);
        String normalizedType = blankToNull(itemType);
        String normalizedStatus = blankToNull(status);
        String normalizedPriority = blankToNull(priority);
        String normalizedKeyword = blankToNull(keyword);
        if (normalizedType != null) parseItemType(normalizedType);
        if (normalizedStatus != null) parseStatus(normalizedStatus);
        if (normalizedPriority != null) parsePriority(normalizedPriority);

        List<ScheduleItem> items = itemMapper.selectTimelineItems(
                rangeStart, rangeEnd, normalizedType, normalizedStatus, normalizedPriority, categoryId, normalizedKeyword);
        return enrichAll(items);
    }

    public List<ScheduleItemResponse> listInbox(String status, String priority, Long categoryId, String keyword) {
        String normalizedStatus = blankToNull(status);
        String normalizedPriority = blankToNull(priority);
        if (normalizedStatus != null) parseStatus(normalizedStatus);
        if (normalizedPriority != null) parsePriority(normalizedPriority);
        return enrichAll(itemMapper.selectInboxItems(normalizedStatus, normalizedPriority, categoryId, blankToNull(keyword)));
    }

    public ScheduleItemResponse getById(Long id) {
        ScheduleItem item = requireItem(id);
        return enrich(item, loadCategoryMap(), loadPlanTitleMap());
    }

    @Transactional
    public ScheduleItemResponse create(ScheduleItemRequest request) {
        ScheduleItem item = fromRequest(request, null);
        validateBusinessRules(item, null);
        itemMapper.insert(item);
        return getById(item.getId());
    }

    @Transactional
    public ScheduleItemResponse update(Long id, ScheduleItemRequest request) {
        ScheduleItem existing = requireItem(id);
        ScheduleItem item = fromRequest(request, existing);
        item.setId(id);
        validateBusinessRules(item, id);
        itemMapper.update(item);
        return getById(id);
    }

    @Transactional
    public void delete(Long id) {
        ScheduleItem item = requireItem(id);
        if (ScheduleItemType.PLAN.name().equals(item.getItemType())) {
            itemMapper.clearRelatedPlanId(id);
        }
        int rows = itemMapper.softDelete(id);
        if (rows == 0) {
            throw new BusinessException(404, "事项不存在或已删除");
        }
    }

    @Transactional
    public ScheduleItemResponse updateStatus(Long id, String status) {
        requireItem(id);
        ScheduleStatus parsed = parseStatus(status);
        int rows = itemMapper.updateStatus(id, parsed.name());
        if (rows == 0) {
            throw new BusinessException(404, "事项不存在或已删除");
        }
        return getById(id);
    }

    public ScheduleStatisticsResponse statistics(int year, int month) {
        if (month < 1 || month > 12) {
            throw new BusinessException(400, "月份必须在 1-12 之间");
        }
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        LocalDateTime rangeStart = monthStart.atStartOfDay();
        LocalDateTime rangeEnd = monthEnd.atTime(LocalTime.MAX).withNano(0);

        Map<String, Object> row = itemMapper.selectMonthStatistics(rangeStart, rangeEnd);
        int total = toInt(row.get("totalCount"));
        int high = toInt(row.get("highPriorityCount"));
        int inProgress = toInt(row.get("inProgressCount"));
        int completed = toInt(row.get("completedCount"));

        LocalDate today = LocalDate.now(ZONE);
        int remainingDays;
        if (today.isBefore(monthStart)) {
            remainingDays = monthStart.lengthOfMonth();
        } else if (today.isAfter(monthEnd)) {
            remainingDays = 0;
        } else {
            remainingDays = (int) ChronoUnit.DAYS.between(today, monthEnd);
        }
        return new ScheduleStatisticsResponse(total, high, inProgress, completed, remainingDays);
    }

    public List<ScheduleItemResponse> listActivePlans() {
        return enrichAll(itemMapper.selectActivePlans());
    }

    private ScheduleItem fromRequest(ScheduleItemRequest request, ScheduleItem existing) {
        ScheduleItemType type = parseItemType(request.getItemType());
        ScheduleStatus status = StringUtils.hasText(request.getStatus())
                ? parseStatus(request.getStatus())
                : (existing != null ? parseStatus(existing.getStatus()) : ScheduleStatus.TODO);
        SchedulePriority priority = StringUtils.hasText(request.getPriority())
                ? parsePriority(request.getPriority())
                : (existing != null ? parsePriority(existing.getPriority()) : SchedulePriority.MEDIUM);

        ScheduleItem item = new ScheduleItem();
        item.setTitle(request.getTitle().trim());
        item.setItemType(type.name());
        item.setDescription(blankToNull(request.getDescription()));
        item.setStatus(status.name());
        item.setPriority(priority.name());
        item.setStartAt(request.getStartAt());
        item.setEndAt(request.getEndAt());
        item.setDueAt(type == ScheduleItemType.TASK ? request.getDueAt() : null);
        item.setAllDay(Boolean.TRUE.equals(request.getAllDay()) ? 1 : 0);
        item.setCategoryId(request.getCategoryId());
        item.setRelatedPlanId(type == ScheduleItemType.TASK ? request.getRelatedPlanId() : null);
        item.setLocation(type == ScheduleItemType.PLAN ? blankToNull(request.getLocation()) : null);
        item.setSortOrder(request.getSortOrder() != null
                ? request.getSortOrder()
                : (existing != null && existing.getSortOrder() != null ? existing.getSortOrder() : 0));
        item.setDeleted(0);
        return item;
    }

    private void validateBusinessRules(ScheduleItem item, Long selfId) {
        ScheduleItemType type = parseItemType(item.getItemType());

        if (item.getStartAt() != null && item.getEndAt() != null && item.getEndAt().isBefore(item.getStartAt())) {
            throw new BusinessException(400, "结束时间不能早于开始时间");
        }

        if (type == ScheduleItemType.PLAN) {
            if (item.getStartAt() == null || item.getEndAt() == null) {
                throw new BusinessException(400, "计划必须设置开始时间和结束时间");
            }
            if (item.getRelatedPlanId() != null) {
                throw new BusinessException(400, "计划不能关联其他计划");
            }
        }

        if (item.getCategoryId() != null) {
            ScheduleCategory category = categoryMapper.selectById(item.getCategoryId());
            if (category == null || category.getStatus() == null || category.getStatus() != 1) {
                throw new BusinessException(400, "分类不存在或已停用");
            }
            String categoryType = category.getItemType();
            if (categoryType != null
                    && !"ALL".equalsIgnoreCase(categoryType)
                    && !categoryType.equalsIgnoreCase(item.getItemType())) {
                throw new BusinessException(400, "分类与事项类型不匹配");
            }
        }

        if (item.getRelatedPlanId() != null) {
            if (selfId != null && Objects.equals(selfId, item.getRelatedPlanId())) {
                throw new BusinessException(400, "事项不能关联自身");
            }
            ScheduleItem plan = itemMapper.selectById(item.getRelatedPlanId());
            if (plan == null) {
                throw new BusinessException(400, "关联的计划不存在");
            }
            if (!ScheduleItemType.PLAN.name().equals(plan.getItemType())) {
                throw new BusinessException(400, "只能关联计划类型事项");
            }
            // Prevent simple cycle: a plan pointing somehow via related_plan_id is already forbidden for PLAN.
            // For TASK, related_plan_id must be PLAN, so cycles of length >1 cannot form through this field alone.
        }
    }

    private ScheduleItem requireItem(Long id) {
        if (id == null) {
            throw new BusinessException(400, "事项 ID 不能为空");
        }
        ScheduleItem item = itemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException(404, "事项不存在或已删除");
        }
        return item;
    }

    private List<ScheduleItemResponse> enrichAll(List<ScheduleItem> items) {
        Map<Long, ScheduleCategory> categories = loadCategoryMap();
        Map<Long, String> planTitles = loadPlanTitleMap();
        return items.stream().map(item -> enrich(item, categories, planTitles)).toList();
    }

    private ScheduleItemResponse enrich(ScheduleItem item,
                                        Map<Long, ScheduleCategory> categories,
                                        Map<Long, String> planTitles) {
        ScheduleItemResponse response = new ScheduleItemResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setItemType(item.getItemType());
        response.setDescription(item.getDescription());
        response.setStatus(item.getStatus());
        response.setPriority(item.getPriority());
        response.setStartAt(item.getStartAt());
        response.setEndAt(item.getEndAt());
        response.setDueAt(item.getDueAt());
        response.setAllDay(item.getAllDay() != null && item.getAllDay() == 1);
        response.setCategoryId(item.getCategoryId());
        if (item.getCategoryId() != null) {
            ScheduleCategory category = categories.get(item.getCategoryId());
            if (category != null) {
                response.setCategoryName(category.getName());
                response.setCategoryColor(category.getColor());
            }
        }
        response.setRelatedPlanId(item.getRelatedPlanId());
        if (item.getRelatedPlanId() != null) {
            String planTitle = planTitles.get(item.getRelatedPlanId());
            if (planTitle == null) {
                ScheduleItem plan = itemMapper.selectById(item.getRelatedPlanId());
                if (plan != null) {
                    planTitle = plan.getTitle();
                }
            }
            response.setRelatedPlanTitle(planTitle);
        }
        response.setLocation(item.getLocation());
        response.setSortOrder(item.getSortOrder());
        response.setDurationDays(computeDurationDays(item));
        response.setCreateTime(item.getCreateTime());
        response.setUpdateTime(item.getUpdateTime());
        return response;
    }

    private Integer computeDurationDays(ScheduleItem item) {
        LocalDate start = toDate(item.getStartAt());
        LocalDate end = toDate(item.getEndAt());
        if (start != null && end != null) {
            return (int) ChronoUnit.DAYS.between(start, end) + 1;
        }
        if (item.getDueAt() != null) {
            return 1;
        }
        if (start != null || end != null) {
            return 1;
        }
        return null;
    }

    private LocalDate toDate(LocalDateTime value) {
        return value == null ? null : value.toLocalDate();
    }

    private Map<Long, ScheduleCategory> loadCategoryMap() {
        Map<Long, ScheduleCategory> map = new HashMap<>();
        for (ScheduleCategory category : categoryMapper.selectList(null, true)) {
            map.put(category.getId(), category);
        }
        return map;
    }

    private Map<Long, String> loadPlanTitleMap() {
        Map<Long, String> map = new HashMap<>();
        for (ScheduleItem plan : itemMapper.selectActivePlans()) {
            map.put(plan.getId(), plan.getTitle());
        }
        // Also include cancelled/completed plans still referenced — load by scanning is enough via selectActivePlans
        // for form selectors; for titles of cancelled plans, fall back to direct lookup in enrich if missing.
        return map;
    }

    private static ScheduleItemType parseItemType(String value) {
        try {
            return ScheduleItemType.from(value);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "无效的事项类型");
        }
    }

    private static ScheduleStatus parseStatus(String value) {
        try {
            return ScheduleStatus.from(value);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "无效的事项状态");
        }
    }

    private static SchedulePriority parsePriority(String value) {
        try {
            return SchedulePriority.from(value);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(400, "无效的优先级");
        }
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private static int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(value.toString());
    }
}
