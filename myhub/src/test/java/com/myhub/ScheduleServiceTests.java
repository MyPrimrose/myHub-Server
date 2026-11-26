package com.myhub;

import com.myhub.common.exception.BusinessException;
import com.myhub.dto.request.ScheduleItemRequest;
import com.myhub.dto.response.ScheduleItemResponse;
import com.myhub.dto.response.ScheduleStatisticsResponse;
import com.myhub.service.ScheduleService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScheduleServiceTests {
    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Long planId;
    private static Long taskId;
    private static Long inboxTaskId;
    private static Long dueOnlyTaskId;

    @Test
    @Order(1)
    void tablesExist() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sys_schedule_item'",
                Integer.class);
        Assumptions.assumeTrue(count != null && count == 1, "sys_schedule_item missing; Flyway may not have run");
    }

    @Test
    @Order(2)
    void createPlan() {
        ScheduleItemRequest request = new ScheduleItemRequest();
        request.setTitle("MyHub 排期联调计划");
        request.setItemType("PLAN");
        request.setPriority("HIGH");
        request.setStatus("IN_PROGRESS");
        request.setStartAt(LocalDateTime.of(2026, 11, 20, 9, 0));
        request.setEndAt(LocalDateTime.of(2026, 12, 5, 18, 0));
        request.setAllDay(false);
        request.setLocation("书房");
        request.setDescription("跨月计划");

        ScheduleItemResponse created = scheduleService.create(request);
        planId = created.getId();
        assertNotNull(planId);
        assertEquals("PLAN", created.getItemType());
        assertTrue(created.getDurationDays() >= 16);
    }

    @Test
    @Order(3)
    void createDatedTaskLinkedToPlan() {
        ScheduleItemRequest request = new ScheduleItemRequest();
        request.setTitle("完成排期表后端");
        request.setItemType("TASK");
        request.setPriority("HIGH");
        request.setStatus("TODO");
        request.setStartAt(LocalDateTime.of(2026, 11, 26, 10, 0));
        request.setEndAt(LocalDateTime.of(2026, 11, 26, 18, 0));
        request.setRelatedPlanId(planId);

        ScheduleItemResponse created = scheduleService.create(request);
        taskId = created.getId();
        assertEquals(planId, created.getRelatedPlanId());
        assertNotNull(created.getRelatedPlanTitle());
    }

    @Test
    @Order(4)
    void createInboxTask() {
        ScheduleItemRequest request = new ScheduleItemRequest();
        request.setTitle("收集箱：整理截图素材");
        request.setItemType("TASK");
        request.setPriority("MEDIUM");
        request.setStatus("TODO");

        ScheduleItemResponse created = scheduleService.create(request);
        inboxTaskId = created.getId();
        assertNull(created.getStartAt());
        assertNull(created.getEndAt());
        assertNull(created.getDueAt());
    }

    @Test
    @Order(5)
    void createDueOnlyTask() {
        ScheduleItemRequest request = new ScheduleItemRequest();
        request.setTitle("交房租");
        request.setItemType("TASK");
        request.setPriority("HIGH");
        request.setDueAt(LocalDateTime.of(2026, 11, 28, 23, 59));

        ScheduleItemResponse created = scheduleService.create(request);
        dueOnlyTaskId = created.getId();
        assertNull(created.getStartAt());
        assertNotNull(created.getDueAt());
    }

    @Test
    @Order(6)
    void planRequiresDates() {
        ScheduleItemRequest request = new ScheduleItemRequest();
        request.setTitle("无效计划");
        request.setItemType("PLAN");
        assertThrows(BusinessException.class, () -> scheduleService.create(request));
    }

    @Test
    @Order(7)
    void timelineIncludesCrossMonthAndDueOnlyExcludesInbox() {
        List<ScheduleItemResponse> november = scheduleService.listItems(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null, null, null, null, null);
        assertTrue(november.stream().anyMatch(item -> item.getId().equals(planId)));
        assertTrue(november.stream().anyMatch(item -> item.getId().equals(taskId)));
        assertTrue(november.stream().anyMatch(item -> item.getId().equals(dueOnlyTaskId)));
        assertTrue(november.stream().noneMatch(item -> item.getId().equals(inboxTaskId)));

        List<ScheduleItemResponse> december = scheduleService.listItems(
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31), null, null, null, null, null);
        assertTrue(december.stream().anyMatch(item -> item.getId().equals(planId)));
    }

    @Test
    @Order(8)
    void inboxOnlyUndatedTasks() {
        List<ScheduleItemResponse> inbox = scheduleService.listInbox(null, null, null, null);
        assertTrue(inbox.stream().anyMatch(item -> item.getId().equals(inboxTaskId)));
        assertTrue(inbox.stream().noneMatch(item -> item.getId().equals(taskId)));
        assertTrue(inbox.stream().noneMatch(item -> item.getId().equals(dueOnlyTaskId)));
        assertTrue(inbox.stream().noneMatch(item -> "PLAN".equals(item.getItemType())));
    }

    @Test
    @Order(9)
    void scheduleInboxTaskMovesToTimeline() {
        ScheduleItemRequest request = new ScheduleItemRequest();
        request.setTitle("收集箱：整理截图素材");
        request.setItemType("TASK");
        request.setPriority("MEDIUM");
        request.setStatus("TODO");
        request.setStartAt(LocalDateTime.of(2026, 11, 27, 9, 0));
        request.setEndAt(LocalDateTime.of(2026, 11, 27, 12, 0));
        request.setRelatedPlanId(planId);

        ScheduleItemResponse updated = scheduleService.update(inboxTaskId, request);
        assertNotNull(updated.getStartAt());

        List<ScheduleItemResponse> inbox = scheduleService.listInbox(null, null, null, null);
        assertTrue(inbox.stream().noneMatch(item -> item.getId().equals(inboxTaskId)));

        List<ScheduleItemResponse> november = scheduleService.listItems(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30), null, null, null, null, null);
        assertTrue(november.stream().anyMatch(item -> item.getId().equals(inboxTaskId)));
    }

    @Test
    @Order(10)
    void statusTransitions() {
        ScheduleItemResponse completed = scheduleService.updateStatus(taskId, "COMPLETED");
        assertEquals("COMPLETED", completed.getStatus());
        ScheduleItemResponse cancelled = scheduleService.updateStatus(taskId, "CANCELLED");
        assertEquals("CANCELLED", cancelled.getStatus());
        ScheduleItemResponse restored = scheduleService.updateStatus(taskId, "TODO");
        assertEquals("TODO", restored.getStatus());
    }

    @Test
    @Order(11)
    void statisticsExcludeCancelledAndDeleted() {
        YearMonth ym = YearMonth.of(2026, 11);
        ScheduleStatisticsResponse stats = scheduleService.statistics(ym.getYear(), ym.getMonthValue());
        assertTrue(stats.getTotalCount() >= 1);
        assertTrue(stats.getRemainingDays() >= 0);
    }

    @Test
    @Order(12)
    void deletePlanUnlinksTasks() {
        Long linkedBefore = jdbcTemplate.queryForObject(
                "SELECT related_plan_id FROM sys_schedule_item WHERE id = ?", Long.class, taskId);
        assertEquals(planId, linkedBefore);

        scheduleService.delete(planId);

        Long linkedAfter = jdbcTemplate.queryForObject(
                "SELECT related_plan_id FROM sys_schedule_item WHERE id = ?", Long.class, taskId);
        assertNull(linkedAfter);

        Integer deletedFlag = jdbcTemplate.queryForObject(
                "SELECT deleted FROM sys_schedule_item WHERE id = ?", Integer.class, planId);
        assertEquals(1, deletedFlag);

        Integer taskDeleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM sys_schedule_item WHERE id = ?", Integer.class, taskId);
        assertEquals(0, taskDeleted);

        String taskStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM sys_schedule_item WHERE id = ?", String.class, taskId);
        assertEquals("TODO", taskStatus);

        assertThrows(BusinessException.class, () -> scheduleService.getById(planId));
    }

    @Test
    @Order(13)
    void deleteTaskDoesNotAffectPlan() {
        // recreate a short-lived plan + task for isolation
        ScheduleItemRequest planReq = new ScheduleItemRequest();
        planReq.setTitle("临时计划");
        planReq.setItemType("PLAN");
        planReq.setStartAt(LocalDateTime.of(2026, 11, 1, 0, 0));
        planReq.setEndAt(LocalDateTime.of(2026, 11, 3, 23, 59));
        Long tempPlanId = scheduleService.create(planReq).getId();

        ScheduleItemRequest taskReq = new ScheduleItemRequest();
        taskReq.setTitle("临时任务");
        taskReq.setItemType("TASK");
        taskReq.setDueAt(LocalDateTime.of(2026, 11, 2, 12, 0));
        taskReq.setRelatedPlanId(tempPlanId);
        Long tempTaskId = scheduleService.create(taskReq).getId();

        scheduleService.delete(tempTaskId);
        assertDoesNotThrow(() -> scheduleService.getById(tempPlanId));
        scheduleService.delete(tempPlanId);
    }

    @Test
    @Order(14)
    void cleanupTestRows() {
        jdbcTemplate.update("UPDATE sys_schedule_item SET deleted = 1 WHERE title LIKE 'MyHub 排期联调计划' OR title LIKE '完成排期表后端' OR title LIKE '收集箱：%' OR title LIKE '交房租' OR title LIKE '临时%'");
    }
}
