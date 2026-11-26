package com.myhub.controller;

import com.myhub.common.result.Result;
import com.myhub.dto.request.LibraryFolderRequest;
import com.myhub.dto.request.LibraryMoveRequest;
import com.myhub.dto.response.LibraryFolderNodeResponse;
import com.myhub.service.LibraryFolderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/library/folders")
public class LibraryFolderController {
    private final LibraryFolderService folderService;

    public LibraryFolderController(LibraryFolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping("/tree")
    public Result<List<LibraryFolderNodeResponse>> tree() {
        return Result.success(folderService.tree());
    }

    @PostMapping
    public Result<LibraryFolderNodeResponse> create(@Valid @RequestBody LibraryFolderRequest request) {
        return Result.success(folderService.create(request));
    }

    @PutMapping("/{id}")
    public Result<LibraryFolderNodeResponse> rename(@PathVariable Long id,
                                                    @Valid @RequestBody LibraryFolderRequest request) {
        return Result.success(folderService.rename(id, request));
    }

    @PutMapping("/{id}/move")
    public Result<Void> move(@PathVariable Long id, @RequestBody LibraryMoveRequest request) {
        folderService.move(id, request.getTargetFolderId());
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestParam(defaultValue = "EMPTY_ONLY") String mode) {
        folderService.delete(id, mode);
        return Result.success(null);
    }
}
