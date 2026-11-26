package com.myhub.controller;

import com.myhub.common.result.Result;
import com.myhub.dto.request.ScheduleCategoryRequest;
import com.myhub.dto.response.ScheduleCategoryResponse;
import com.myhub.service.ScheduleCategoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedule/categories")
public class ScheduleCategoryController {
    private final ScheduleCategoryService categoryService;

    public ScheduleCategoryController(ScheduleCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<ScheduleCategoryResponse>> list(
            @RequestParam(required = false) String itemType,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(categoryService.list(itemType, includeDisabled));
    }

    @GetMapping("/{id}")
    public Result<ScheduleCategoryResponse> get(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @PostMapping
    public Result<ScheduleCategoryResponse> create(@Valid @RequestBody ScheduleCategoryRequest request) {
        return Result.success(categoryService.create(request));
    }

    @PutMapping("/{id}")
    public Result<ScheduleCategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody ScheduleCategoryRequest request) {
        return Result.success(categoryService.update(id, request));
    }

    @PatchMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        categoryService.disable(id);
        return Result.success(null);
    }
}
