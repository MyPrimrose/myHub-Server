package com.myhub.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HealthMapper {
    int pingDatabase();
}
