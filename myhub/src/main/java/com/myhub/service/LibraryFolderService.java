package com.myhub.service;

import com.myhub.common.exception.BusinessException;
import com.myhub.dto.request.LibraryFolderRequest;
import com.myhub.dto.response.LibraryFileResponse;
import com.myhub.dto.response.LibraryFolderNodeResponse;
import com.myhub.entity.LibraryFile;
import com.myhub.entity.LibraryFolder;
import com.myhub.mapper.LibraryFileMapper;
import com.myhub.mapper.LibraryFolderMapper;
import com.myhub.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class LibraryFolderService {
    private final LibraryFolderMapper folderMapper;
    private final LibraryFileMapper fileMapper;

    public LibraryFolderService(LibraryFolderMapper folderMapper, LibraryFileMapper fileMapper) {
        this.folderMapper = folderMapper;
        this.fileMapper = fileMapper;
    }

    public List<LibraryFolderNodeResponse> tree() {
        long ownerId = SecurityUtils.requireCurrentUserId();
        List<LibraryFolder> folders = folderMapper.selectByOwner(ownerId);
        Map<Long, LibraryFolderNodeResponse> nodes = new HashMap<>();
        for (LibraryFolder folder : folders) {
            LibraryFolderNodeResponse node = toNode(folder);
            node.setFileCount(fileMapper.countByFolder(ownerId, folder.getId()));
            nodes.put(folder.getId(), node);
        }
        List<LibraryFolderNodeResponse> roots = new ArrayList<>();
        for (LibraryFolder folder : folders) {
            LibraryFolderNodeResponse node = nodes.get(folder.getId());
            if (folder.getParentId() == null) {
                roots.add(node);
            } else {
                LibraryFolderNodeResponse parent = nodes.get(folder.getParentId());
                if (parent != null) parent.getChildren().add(node);
                else roots.add(node);
            }
        }
        for (LibraryFolder folder : folders) {
            List<LibraryFile> files = fileMapper.selectByFolder(ownerId, folder.getId());
            nodes.get(folder.getId()).setFiles(files.stream().map(this::toFileBrief).toList());
        }
        return roots;
    }

    @Transactional
    public LibraryFolderNodeResponse create(LibraryFolderRequest request) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        String name = request.getName().trim();
        Long parentId = request.getParentId();
        if (parentId != null) requireFolder(parentId, ownerId);
        ensureUniqueName(ownerId, parentId, name, null);
        LibraryFolder folder = new LibraryFolder();
        folder.setOwnerId(ownerId);
        folder.setName(name);
        folder.setParentId(parentId);
        folder.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        folder.setDeleted(0);
        folderMapper.insert(folder);
        return toNode(requireFolder(folder.getId(), ownerId));
    }

    @Transactional
    public LibraryFolderNodeResponse rename(Long id, LibraryFolderRequest request) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        LibraryFolder existing = requireFolder(id, ownerId);
        String name = request.getName().trim();
        ensureUniqueName(ownerId, existing.getParentId(), name, id);
        existing.setName(name);
        if (request.getSortOrder() != null) existing.setSortOrder(request.getSortOrder());
        folderMapper.update(existing);
        return toNode(requireFolder(id, ownerId));
    }

    @Transactional
    public void move(Long id, Long targetParentId) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        LibraryFolder folder = requireFolder(id, ownerId);
        if (Objects.equals(id, targetParentId)) {
            throw new BusinessException(400, "不能将文件夹移动到自身");
        }
        if (targetParentId != null) {
            requireFolder(targetParentId, ownerId);
            if (isDescendant(ownerId, id, targetParentId)) {
                throw new BusinessException(400, "不能将文件夹移动到其子目录中");
            }
        }
        ensureUniqueName(ownerId, targetParentId, folder.getName(), id);
        folderMapper.updateParent(id, ownerId, targetParentId);
    }

    @Transactional
    public void delete(Long id, String mode) {
        long ownerId = SecurityUtils.requireCurrentUserId();
        LibraryFolder folder = requireFolder(id, ownerId);
        String action = StringUtils.hasText(mode) ? mode.trim().toUpperCase() : "EMPTY_ONLY";
        int childFolders = folderMapper.countChildren(ownerId, id);
        int childFiles = fileMapper.countByFolder(ownerId, id);
        Long parentId = folder.getParentId();

        if ("EMPTY_ONLY".equals(action)) {
            if (childFolders > 0 || childFiles > 0) {
                throw new BusinessException(400, "文件夹非空，请先清空或选择将内容上移后删除");
            }
            folderMapper.softDelete(id, ownerId);
            return;
        }
        if ("MOVE_UP".equals(action)) {
            if (childFolders > 0) {
                List<LibraryFolder> all = folderMapper.selectByOwner(ownerId);
                for (LibraryFolder child : all) {
                    if (Objects.equals(child.getParentId(), id)) {
                        ensureUniqueName(ownerId, parentId, child.getName(), child.getId());
                        folderMapper.updateParent(child.getId(), ownerId, parentId);
                    }
                }
            }
            if (childFiles > 0) {
                fileMapper.moveFolderFilesToParent(ownerId, id, parentId);
            }
            folderMapper.softDelete(id, ownerId);
            return;
        }
        throw new BusinessException(400, "不支持的删除模式");
    }

    public LibraryFolder requireFolder(Long id, long ownerId) {
        LibraryFolder folder = folderMapper.selectById(id, ownerId);
        if (folder == null) throw new BusinessException(404, "文件夹不存在");
        return folder;
    }

    private void ensureUniqueName(long ownerId, Long parentId, String name, Long excludeId) {
        LibraryFolder conflict = folderMapper.selectByParentAndName(ownerId, parentId, name);
        if (conflict != null && (excludeId == null || !Objects.equals(conflict.getId(), excludeId))) {
            throw new BusinessException(400, "同一目录下已存在同名文件夹");
        }
    }

    private boolean isDescendant(long ownerId, Long ancestorId, Long candidateId) {
        Map<Long, Long> parentMap = new HashMap<>();
        for (LibraryFolder folder : folderMapper.selectByOwner(ownerId)) {
            parentMap.put(folder.getId(), folder.getParentId());
        }
        Set<Long> seen = new HashSet<>();
        Long current = candidateId;
        while (current != null && seen.add(current)) {
            if (Objects.equals(current, ancestorId)) return true;
            current = parentMap.get(current);
        }
        return false;
    }

    private LibraryFolderNodeResponse toNode(LibraryFolder folder) {
        LibraryFolderNodeResponse node = new LibraryFolderNodeResponse();
        node.setId(folder.getId());
        node.setName(folder.getName());
        node.setParentId(folder.getParentId());
        node.setSortOrder(folder.getSortOrder());
        node.setCreateTime(folder.getCreateTime());
        return node;
    }

    private LibraryFileResponse toFileBrief(LibraryFile file) {
        LibraryFileResponse response = new LibraryFileResponse();
        response.setId(file.getId());
        response.setFolderId(file.getFolderId());
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
}
