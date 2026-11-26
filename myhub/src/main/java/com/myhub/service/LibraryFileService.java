package com.myhub.service;

import com.myhub.common.exception.BusinessException;
import com.myhub.config.LibraryProperties;
import com.myhub.dto.request.LibraryFileUpdateRequest;
import com.myhub.dto.response.LibraryFileResponse;
import com.myhub.dto.response.LibraryPreviewResponse;
import com.myhub.dto.response.LibraryStatisticsResponse;
import com.myhub.dto.response.PageResponse;
import com.myhub.entity.LibraryFile;
import com.myhub.entity.LibraryFolder;
import com.myhub.mapper.LibraryFileMapper;
import com.myhub.mapper.LibraryFolderMapper;
import com.myhub.util.SecurityUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class LibraryFileService {
    private static final Set<String> ALLOWED_EXT = Set.of("html", "htm");
    private static final Set<String> ALLOWED_MIME = Set.of(
            "text/html", "application/xhtml+xml", "text/plain", "application/octet-stream");

    private final LibraryFileMapper fileMapper;
    private final LibraryFolderMapper folderMapper;
    private final StorageService storageService;
    private final LibraryProperties libraryProperties;

    public LibraryFileService(LibraryFileMapper fileMapper,
                              LibraryFolderMapper folderMapper,
                              StorageService storageService,
                              LibraryProperties libraryProperties) {
        this.fileMapper = fileMapper;
        this.folderMapper = folderMapper;
        this.storageService = storageService;
        this.libraryProperties = libraryProperties;
    }

    public PageResponse<LibraryFileResponse> list(Long folderId, boolean uncategorized, String keyword,
                                                  Boolean favorite, int page, int size) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;
        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        if (!uncategorized && folderId != null) {
            requireOwnedFolder(folderId, ownerId);
        }
        long total = fileMapper.countPage(ownerId, folderId, uncategorized, kw, favorite);
        List<LibraryFileResponse> records = fileMapper
                .selectPage(ownerId, folderId, uncategorized, kw, favorite, offset, safeSize)
                .stream().map(this::toResponse).toList();
        return new PageResponse<>(records, total, safePage, safeSize);
    }

    public LibraryFileResponse get(Long id) {
        return toResponse(requireFile(id, SecurityUtils.requireCurrentUserId()));
    }

    @Transactional
    public LibraryFileResponse upload(MultipartFile file, Long folderId, String conflictStrategy) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的文件");
        }
        if (file.getSize() > libraryProperties.getMaxFileBytes()) {
            throw new BusinessException(400, "单个文件不能超过 10MB");
        }
        long used = nullToZero(fileMapper.sumSizeByOwner(ownerId));
        if (used + file.getSize() > libraryProperties.getMaxStorageBytes()) {
            throw new BusinessException(400, "存储空间不足");
        }
        if (folderId != null) requireOwnedFolder(folderId, ownerId);

        String original = sanitizeOriginalName(file.getOriginalFilename());
        String ext = extensionOf(original);
        validateExtension(ext);
        validateMime(file.getContentType());

        String displayName = original;
        String strategy = StringUtils.hasText(conflictStrategy)
                ? conflictStrategy.trim().toUpperCase(Locale.ROOT) : "REJECT";
        LibraryFile existing = fileMapper.selectByFolderAndName(ownerId, folderId, displayName);

        if (existing != null) {
            if ("REJECT".equals(strategy)) {
                throw new BusinessException(409, "同名文件已存在，请选择重命名或覆盖");
            }
            if ("RENAME".equals(strategy)) {
                displayName = uniqueName(ownerId, folderId, displayName);
            } else if ("OVERWRITE".equals(strategy)) {
                return overwrite(existing, file, original, ext);
            } else {
                throw new BusinessException(400, "不支持的冲突处理策略");
            }
        }

        StorageService.StoredObject stored;
        try {
            stored = storageService.storeLibraryHtml(file.getInputStream(), ext);
        } catch (IOException ex) {
            throw new BusinessException(500, "读取上传文件失败");
        }

        LibraryFile entity = new LibraryFile();
        entity.setOwnerId(ownerId);
        entity.setFolderId(folderId);
        entity.setName(displayName);
        entity.setOriginalName(original);
        entity.setStorageKey(stored.storageKey());
        entity.setFileExtension(ext);
        entity.setMimeType(normalizeMime(file.getContentType()));
        entity.setFileSize(stored.size());
        entity.setContentHash(stored.contentHash());
        entity.setIsFavorite(0);
        entity.setAccessCount(0);
        entity.setDeleted(0);
        fileMapper.insert(entity);
        return toResponse(requireFile(entity.getId(), ownerId));
    }

    @Transactional
    public LibraryFileResponse rename(Long id, LibraryFileUpdateRequest request) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        LibraryFile file = requireFile(id, ownerId);
        String name = ensureHtmlName(request.getName().trim());
        LibraryFile conflict = fileMapper.selectByFolderAndName(ownerId, file.getFolderId(), name);
        if (conflict != null && !Objects.equals(conflict.getId(), id)) {
            throw new BusinessException(400, "目标目录已存在同名文件");
        }
        file.setName(name);
        fileMapper.update(file);
        return toResponse(requireFile(id, ownerId));
    }

    @Transactional
    public LibraryFileResponse move(Long id, Long targetFolderId) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        LibraryFile file = requireFile(id, ownerId);
        if (targetFolderId != null) requireOwnedFolder(targetFolderId, ownerId);
        LibraryFile conflict = fileMapper.selectByFolderAndName(ownerId, targetFolderId, file.getName());
        if (conflict != null && !Objects.equals(conflict.getId(), id)) {
            throw new BusinessException(400, "目标目录已存在同名文件");
        }
        fileMapper.updateFolder(id, ownerId, targetFolderId);
        return toResponse(requireFile(id, ownerId));
    }

    @Transactional
    public LibraryFileResponse favorite(Long id, boolean favorite) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        requireFile(id, ownerId);
        fileMapper.updateFavorite(id, ownerId, favorite ? 1 : 0);
        return toResponse(requireFile(id, ownerId));
    }

    @Transactional
    public void delete(Long id) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        LibraryFile file = requireFile(id, ownerId);
        fileMapper.softDelete(id, ownerId);
        // Disk cleanup after DB success; orphan files are acceptable if this fails.
        storageService.deleteQuietly(file.getStorageKey());
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(400, "请选择要删除的文件");
        }
        long ownerId = SecurityUtils.requireCurrentUserId();
        List<String> keys = new ArrayList<>();
        for (Long id : ids) {
            LibraryFile file = fileMapper.selectById(id, ownerId);
            if (file != null) keys.add(file.getStorageKey());
        }
        fileMapper.softDeleteBatch(ids, ownerId);
        keys.forEach(storageService::deleteQuietly);
    }

    @Transactional
    public LibraryPreviewResponse preview(Long id) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        LibraryFile file = requireFile(id, ownerId);
        fileMapper.markAccessed(id, ownerId);
        String content = storageService.readHtmlText(file.getStorageKey());
        // Frontend renders via sandboxed blob iframe without allow-same-origin.
        return new LibraryPreviewResponse(
                file.getId(), file.getName(), file.getFileSize(), content, "SANDBOXED_BLOB");
    }

    public ResponseEntity<Resource> download(Long id) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        LibraryFile file = requireFile(id, ownerId);
        byte[] bytes = storageService.readBytes(file.getStorageKey());
        String encoded = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    public List<LibraryFileResponse> recent(int limit) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        return fileMapper.selectRecent(ownerId, Math.min(Math.max(limit, 1), 50))
                .stream().map(this::toResponse).toList();
    }

    public List<LibraryFileResponse> favorites(int limit) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        return fileMapper.selectFavorites(ownerId, Math.min(Math.max(limit, 1), 100))
                .stream().map(this::toResponse).toList();
    }

    public LibraryStatisticsResponse statistics() {
        long ownerId = SecurityUtils.requireCurrentUserId();
        return new LibraryStatisticsResponse(
                folderMapper.countByOwner(ownerId),
                fileMapper.countByOwner(ownerId),
                nullToZero(fileMapper.sumSizeByOwner(ownerId)),
                libraryProperties.getMaxStorageBytes());
    }

    public LibraryFileResponse toResponse(LibraryFile file) {
        LibraryFileResponse response = new LibraryFileResponse();
        response.setId(file.getId());
        response.setFolderId(file.getFolderId());
        if (file.getFolderId() != null) {
            LibraryFolder folder = folderMapper.selectById(file.getFolderId(), file.getOwnerId());
            if (folder != null) response.setFolderName(folder.getName());
        } else {
            response.setFolderName("未归类的页面");
        }
        response.setName(file.getName());
        response.setOriginalName(file.getOriginalName());
        response.setFileExtension(file.getFileExtension());
        response.setMimeType(file.getMimeType());
        response.setFileSize(file.getFileSize());
        response.setFavorite(file.getIsFavorite() != null && file.getIsFavorite() == 1);
        response.setLastAccessTime(file.getLastAccessTime());
        response.setAccessCount(file.getAccessCount());
        response.setCreateTime(file.getCreateTime());
        response.setUpdateTime(file.getUpdateTime());
        return response;
    }

    private LibraryFileResponse overwrite(LibraryFile existing, MultipartFile file, String original, String ext) {
        StorageService.StoredObject stored;
        try {
            stored = storageService.storeLibraryHtml(file.getInputStream(), ext);
        } catch (IOException ex) {
            throw new BusinessException(500, "读取上传文件失败");
        }
        String oldKey = existing.getStorageKey();
        existing.setOriginalName(original);
        existing.setStorageKey(stored.storageKey());
        existing.setFileExtension(ext);
        existing.setMimeType(normalizeMime(file.getContentType()));
        existing.setFileSize(stored.size());
        existing.setContentHash(stored.contentHash());
        fileMapper.update(existing);
        if (!Objects.equals(oldKey, stored.storageKey())) {
            storageService.deleteQuietly(oldKey);
        }
        return toResponse(requireFile(existing.getId(), existing.getOwnerId()));
    }

    private LibraryFile requireFile(Long id, long ownerId) {
        LibraryFile file = fileMapper.selectById(id, ownerId);
        if (file == null) throw new BusinessException(404, "文件不存在");
        return file;
    }

    private void requireOwnedFolder(Long folderId, long ownerId) {
        if (folderMapper.selectById(folderId, ownerId) == null) {
            throw new BusinessException(404, "文件夹不存在");
        }
    }

    private String uniqueName(long ownerId, Long folderId, String original) {
        String base = original;
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > 0) {
            base = original.substring(0, dot);
            ext = original.substring(dot);
        }
        int index = 1;
        String candidate = original;
        while (fileMapper.selectByFolderAndName(ownerId, folderId, candidate) != null) {
            candidate = base + " (" + index + ")" + ext;
            index++;
        }
        return candidate;
    }

    private static String sanitizeOriginalName(String filename) {
        if (!StringUtils.hasText(filename)) {
            throw new BusinessException(400, "文件名无效");
        }
        String name = filename.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        name = name.trim();
        if (!StringUtils.hasText(name) || name.contains("..")) {
            throw new BusinessException(400, "文件名非法");
        }
        if (name.length() > 200) name = name.substring(0, 200);
        return name;
    }

    private static String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new BusinessException(400, "仅支持 .html / .htm 文件");
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static void validateExtension(String ext) {
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(400, "仅支持 .html / .htm 文件");
        }
    }

    private static void validateMime(String mime) {
        if (!StringUtils.hasText(mime)) return;
        String normalized = mime.toLowerCase(Locale.ROOT).split(";")[0].trim();
        if (!ALLOWED_MIME.contains(normalized)) {
            throw new BusinessException(400, "文件类型不被允许");
        }
    }

    private static String normalizeMime(String mime) {
        if (!StringUtils.hasText(mime)) return "text/html";
        return mime.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private static String ensureHtmlName(String name) {
        String trimmed = name.trim();
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".html") && !lower.endsWith(".htm")) {
            trimmed = trimmed + ".html";
        }
        if (trimmed.contains("/") || trimmed.contains("\\") || trimmed.contains("..")) {
            throw new BusinessException(400, "文件名非法");
        }
        return trimmed;
    }

    private static long nullToZero(Long value) {
        return value == null ? 0L : value;
    }
}
