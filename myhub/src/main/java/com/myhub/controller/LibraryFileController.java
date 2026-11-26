package com.myhub.controller;

import com.myhub.common.result.Result;
import com.myhub.dto.request.LibraryBatchDeleteRequest;
import com.myhub.dto.request.LibraryFavoriteRequest;
import com.myhub.dto.request.LibraryFileUpdateRequest;
import com.myhub.dto.request.LibraryMoveRequest;
import com.myhub.dto.response.LibraryFileResponse;
import com.myhub.dto.response.LibraryPreviewResponse;
import com.myhub.dto.response.LibraryStatisticsResponse;
import com.myhub.dto.response.PageResponse;
import com.myhub.service.LibraryFileService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryFileController {
    private final LibraryFileService fileService;

    public LibraryFileController(LibraryFileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files/upload")
    public Result<LibraryFileResponse> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam(required = false) Long folderId,
                                              @RequestParam(defaultValue = "REJECT") String conflictStrategy) {
        return Result.success(fileService.upload(file, folderId, conflictStrategy));
    }

    @GetMapping("/files")
    public Result<PageResponse<LibraryFileResponse>> list(
            @RequestParam(required = false) Long folderId,
            @RequestParam(defaultValue = "false") boolean uncategorized,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Result.success(fileService.list(folderId, uncategorized, keyword, favorite, page, size));
    }

    @GetMapping("/files/{id}")
    public Result<LibraryFileResponse> detail(@PathVariable Long id) {
        return Result.success(fileService.get(id));
    }

    @GetMapping("/files/{id}/preview")
    public Result<LibraryPreviewResponse> preview(@PathVariable Long id) {
        return Result.success(fileService.preview(id));
    }

    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        return fileService.download(id);
    }

    @PutMapping("/files/{id}")
    public Result<LibraryFileResponse> rename(@PathVariable Long id,
                                              @Valid @RequestBody LibraryFileUpdateRequest request) {
        return Result.success(fileService.rename(id, request));
    }

    @PutMapping("/files/{id}/move")
    public Result<LibraryFileResponse> move(@PathVariable Long id, @RequestBody LibraryMoveRequest request) {
        return Result.success(fileService.move(id, request.getTargetFolderId()));
    }

    @PatchMapping("/files/{id}/favorite")
    public Result<LibraryFileResponse> favorite(@PathVariable Long id,
                                                @Valid @RequestBody LibraryFavoriteRequest request) {
        return Result.success(fileService.favorite(id, Boolean.TRUE.equals(request.getFavorite())));
    }

    @DeleteMapping("/files/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        fileService.delete(id);
        return Result.success(null);
    }

    @DeleteMapping("/files/batch")
    public Result<Void> deleteBatch(@Valid @RequestBody LibraryBatchDeleteRequest request) {
        fileService.deleteBatch(request.getIds());
        return Result.success(null);
    }

    @GetMapping("/statistics")
    public Result<LibraryStatisticsResponse> statistics() {
        return Result.success(fileService.statistics());
    }

    @GetMapping("/recent")
    public Result<List<LibraryFileResponse>> recent(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(fileService.recent(limit));
    }

    @GetMapping("/favorites")
    public Result<List<LibraryFileResponse>> favorites(@RequestParam(defaultValue = "50") int limit) {
        return Result.success(fileService.favorites(limit));
    }
}
