package com.myhub.controller;

import com.myhub.common.result.Result;
import com.myhub.mapper.HealthMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    private final HealthMapper healthMapper;
    public HealthController(HealthMapper healthMapper) { this.healthMapper = healthMapper; }

    @GetMapping
    public Result<Map<String, String>> health() {
        healthMapper.pingDatabase();
        return Result.success(Map.of("status", "UP", "application", "MyHub"));
    }
}
