package com.myhub.mapper;

import com.myhub.entity.ScheduleCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScheduleCategoryMapper {
    ScheduleCategory selectById(@Param("id") Long id);

    List<ScheduleCategory> selectList(@Param("itemType") String itemType, @Param("includeDisabled") boolean includeDisabled);

    int insert(ScheduleCategory category);

    int update(ScheduleCategory category);

    int disable(@Param("id") Long id);
}
