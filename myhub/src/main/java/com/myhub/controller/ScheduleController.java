package com.myhub.controller;

import com.myhub.common.result.Result;
import com.myhub.dto.request.ScheduleItemRequest;
import com.myhub.dto.request.ScheduleStatusRequest;
import com.myhub.dto.response.ScheduleItemResponse;
import com.myhub.dto.response.ScheduleStatisticsResponse;
import com.myhub.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/items")
    public Result<List<ScheduleItemResponse>> listItems(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String itemType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(scheduleService.listItems(
                startDate, endDate, itemType, status, priority, categoryId, keyword));
    }

    @GetMapping("/items/{id}")
    public Result<ScheduleItemResponse> getItem(@PathVariable Long id) {
        return Result.success(scheduleService.getById(id));
    }

    @PostMapping("/items")
    public Result<ScheduleItemResponse> createItem(@Valid @RequestBody ScheduleItemRequest request) {
        return Result.success(scheduleService.create(request));
    }

    @PutMapping("/items/{id}")
    public Result<ScheduleItemResponse> updateItem(@PathVariable Long id,
                                                   @Valid @RequestBody ScheduleItemRequest request) {
        return Result.success(scheduleService.update(id, request));
    }

    @DeleteMapping("/items/{id}")
    public Result<Void> deleteItem(@PathVariable Long id) {
        scheduleService.delete(id);
        return Result.success(null);
    }

    @PatchMapping("/items/{id}/status")
    public Result<ScheduleItemResponse> updateStatus(@PathVariable Long id,
                                                     @Valid @RequestBody ScheduleStatusRequest request) {
        return Result.success(scheduleService.updateStatus(id, request.getStatus()));
    }

    @GetMapping("/statistics")
    public Result<ScheduleStatisticsResponse> statistics(@RequestParam int year, @RequestParam int month) {
        return Result.success(scheduleService.statistics(year, month));
    }

    @GetMapping("/inbox")
    public Result<List<ScheduleItemResponse>> inbox(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(scheduleService.listInbox(status, priority, categoryId, keyword));
    }

    @GetMapping("/plans")
    public Result<List<ScheduleItemResponse>> activePlans() {
        return Result.success(scheduleService.listActivePlans());
    }
}
