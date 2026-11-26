package com.myhub.mapper;

import com.myhub.entity.ScheduleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ScheduleItemMapper {
    ScheduleItem selectById(@Param("id") Long id);

    List<ScheduleItem> selectTimelineItems(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("itemType") String itemType,
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword);

    List<ScheduleItem> selectInboxItems(
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword);

    Map<String, Object> selectMonthStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    int insert(ScheduleItem item);

    int update(ScheduleItem item);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int softDelete(@Param("id") Long id);

    int clearRelatedPlanId(@Param("planId") Long planId);

    List<ScheduleItem> selectActivePlans();
}
