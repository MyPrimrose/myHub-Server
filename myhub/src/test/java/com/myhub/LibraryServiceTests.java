package com.myhub;

import com.myhub.common.exception.BusinessException;
import com.myhub.dto.request.LibraryFolderRequest;
import com.myhub.dto.response.LibraryFileResponse;
import com.myhub.dto.response.LibraryFolderNodeResponse;
import com.myhub.dto.response.LibraryPreviewResponse;
import com.myhub.entity.UserAccount;
import com.myhub.mapper.UserMapper;
import com.myhub.service.LibraryFileService;
import com.myhub.service.LibraryFolderService;
import com.myhub.service.StorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LibraryServiceTests {
    @Autowired private LibraryFolderService folderService;
    @Autowired private LibraryFileService fileService;
    @Autowired private UserMapper userMapper;
    @Autowired private StorageService storageService;

    private static Long folderId;
    private static Long childFolderId;
    private static Long fileId;

    @BeforeEach
    void loginAsSadmin() {
        UserAccount user = userMapper.selectByUsername("sadmin");
        Assumptions.assumeTrue(user != null, "sadmin user required");
        var auth = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test @Order(1)
    void createFolderAndChild() {
        LibraryFolderRequest req = new LibraryFolderRequest();
        req.setName("灵感收集");
        LibraryFolderNodeResponse folder = folderService.create(req);
        folderId = folder.getId();
        assertNotNull(folderId);

        LibraryFolderRequest child = new LibraryFolderRequest();
        child.setName("读书");
        child.setParentId(folderId);
        childFolderId = folderService.create(child).getId();
        assertNotNull(childFolderId);
    }

    @Test @Order(2)
    void uploadHtmlAndRejectTxt() {
        MockMultipartFile html = new MockMultipartFile(
                "file", "本月读书清单.html", "text/html",
                "<html><body><h1>reading</h1></body></html>".getBytes(StandardCharsets.UTF_8));
        LibraryFileResponse uploaded = fileService.upload(html, folderId, "REJECT");
        fileId = uploaded.getId();
        assertEquals("本月读书清单.html", uploaded.getName());
        assertTrue(fileService.preview(fileId).getContent().contains("reading"));

        MockMultipartFile txt = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));
        assertThrows(BusinessException.class, () -> fileService.upload(txt, folderId, "REJECT"));

        MockMultipartFile exe = new MockMultipartFile(
                "file", "malware.exe", "application/octet-stream", new byte[]{0x4D, 0x5A});
        assertThrows(BusinessException.class, () -> fileService.upload(exe, folderId, "REJECT"));
    }

    @Test @Order(3)
    void uploadHtmConflictRenameAndPreview() {
        MockMultipartFile htm = new MockMultipartFile(
                "file", "page.htm", "text/html",
                "<html><body>htm</body></html>".getBytes(StandardCharsets.UTF_8));
        LibraryFileResponse first = fileService.upload(htm, folderId, "REJECT");
        assertTrue(first.getName().endsWith(".htm"));

        MockMultipartFile again = new MockMultipartFile(
                "file", "page.htm", "text/html",
                "<html><body>again</body></html>".getBytes(StandardCharsets.UTF_8));
        assertThrows(BusinessException.class, () -> fileService.upload(again, folderId, "REJECT"));
        LibraryFileResponse renamed = fileService.upload(again, folderId, "RENAME");
        assertNotEquals("page.htm", renamed.getName());

        LibraryPreviewResponse preview = fileService.preview(fileId);
        assertEquals("SANDBOXED_BLOB", preview.getIsolationMode());
        assertTrue(preview.getContent().contains("reading"));
    }

    @Test @Order(4)
    void previewGbkHtmlAndNestedFolder() throws Exception {
        LibraryFolderRequest grand = new LibraryFolderRequest();
        grand.setName("三级目录-" + System.currentTimeMillis());
        grand.setParentId(childFolderId);
        Long grandId = folderService.create(grand).getId();
        assertNotNull(grandId);
        List<LibraryFolderNodeResponse> tree = folderService.tree();
        LibraryFolderNodeResponse root = tree.stream()
                .filter(n -> Objects.equals(n.getId(), folderId))
                .findFirst()
                .orElseThrow();
        assertTrue(root.getChildren().stream()
                .anyMatch(c -> Objects.equals(c.getId(), childFolderId)
                        && c.getChildren().stream().anyMatch(g -> Objects.equals(g.getId(), grandId))));

        byte[] gbk = "<html><head><meta charset=\"gbk\"></head><body>中文预览</body></html>"
                .getBytes(java.nio.charset.Charset.forName("GBK"));
        MockMultipartFile file = new MockMultipartFile(
                "file", "gbk-page.html", "text/html", gbk);
        LibraryFileResponse uploaded = fileService.upload(file, grandId, "REJECT");
        LibraryPreviewResponse preview = fileService.preview(uploaded.getId());
        assertTrue(preview.getContent().contains("中文预览"));
        fileService.delete(uploaded.getId());
        folderService.delete(grandId, "EMPTY_ONLY");
    }

    @Test @Order(5)
    void favoriteMoveAndStats() {
        fileService.favorite(fileId, true);
        assertTrue(fileService.get(fileId).getFavorite());
        fileService.move(fileId, childFolderId);
        assertEquals(childFolderId, fileService.get(fileId).getFolderId());
        assertTrue(fileService.statistics().getFileCount() >= 1);
        assertFalse(fileService.recent(5).isEmpty());
        assertFalse(fileService.favorites(5).isEmpty());
    }

    @Test @Order(6)
    void deleteNonEmptyFolderRequiresMode() {
        assertThrows(BusinessException.class, () -> folderService.delete(folderId, "EMPTY_ONLY"));
        folderService.delete(childFolderId, "MOVE_UP");
        folderService.delete(folderId, "MOVE_UP");
    }

    @Test @Order(7)
    void pathTraversalRejectedByStorage() {
        assertThrows(BusinessException.class, () -> storageService.readBytes("../secret.html"));
        assertThrows(BusinessException.class, () -> storageService.readBytes("library/../../etc/passwd"));
    }

    @Test @Order(8)
    void cleanup() {
        try { fileService.delete(fileId); } catch (Exception ignored) {}
        assertTrue(Files.exists(storageService.rootPath()));
    }
}
