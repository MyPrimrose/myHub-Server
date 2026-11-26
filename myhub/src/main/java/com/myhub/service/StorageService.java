package com.myhub.service;

import com.myhub.common.exception.BusinessException;
import com.myhub.config.StorageProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class StorageService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private final Path root;

    public StorageService(StorageProperties properties) {
        this.root = Path.of(properties.getPath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root.resolve("library"));
        } catch (IOException ex) {
            throw new IllegalStateException("无法初始化存储目录: " + root, ex);
        }
    }

    public Path rootPath() {
        return root;
    }

    public StoredObject storeLibraryHtml(InputStream input, String extension) {
        String ext = normalizeExtension(extension);
        LocalDate today = LocalDate.now(ZONE);
        String relative = "library/%d/%02d/%s.%s".formatted(
                today.getYear(), today.getMonthValue(), UUID.randomUUID().toString().replace("-", ""), ext);
        Path target = resolveSafe(relative);
        try {
            Files.createDirectories(target.getParent());
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long size;
            try (DigestInputStream digester = new DigestInputStream(input, digest)) {
                size = Files.copy(digester, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String hash = HexFormat.of().formatHex(digest.digest());
            return new StoredObject(relative, size, hash);
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new BusinessException(500, "文件保存失败");
        }
    }

    public void overwrite(String storageKey, InputStream input) {
        Path target = resolveSafe(storageKey);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new BusinessException(500, "文件覆盖失败");
        }
    }

    public byte[] readBytes(String storageKey) {
        Path target = resolveSafe(storageKey);
        if (!Files.exists(target)) {
            throw new BusinessException(404, "文件内容不存在");
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException ex) {
            throw new BusinessException(500, "文件读取失败");
        }
    }

    public String readUtf8(String storageKey) {
        return new String(readBytes(storageKey), StandardCharsets.UTF_8);
    }

    /**
     * Decode HTML bytes with charset detection (BOM / meta charset / GBK fallback).
     * Many local HTML pages on Windows are saved as GBK and break under naive UTF-8.
     */
    public String readHtmlText(String storageKey) {
        byte[] bytes = readBytes(storageKey);
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xEF
                && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        if (bytes.length >= 2
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xFE) {
            return new String(bytes, StandardCharsets.UTF_16LE);
        }
        if (bytes.length >= 2
                && (bytes[0] & 0xFF) == 0xFE
                && (bytes[1] & 0xFF) == 0xFF) {
            return new String(bytes, StandardCharsets.UTF_16BE);
        }

        String head = new String(bytes, 0, Math.min(bytes.length, 4096), StandardCharsets.ISO_8859_1);
        String charset = detectCharsetFromMeta(head);
        try {
            if (charset != null) {
                return new String(bytes, java.nio.charset.Charset.forName(charset));
            }
            String utf8 = new String(bytes, StandardCharsets.UTF_8);
            if (looksBrokenChinese(utf8)) {
                return new String(bytes, java.nio.charset.Charset.forName("GBK"));
            }
            return utf8;
        } catch (Exception ex) {
            try {
                return new String(bytes, java.nio.charset.Charset.forName("GBK"));
            } catch (Exception ignored) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }

    private static String detectCharsetFromMeta(String head) {
        java.util.regex.Matcher charsetMatcher = java.util.regex.Pattern
                .compile("charset\\s*=\\s*['\"]?([a-zA-Z0-9._-]+)", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(head);
        if (charsetMatcher.find()) {
            String value = charsetMatcher.group(1).trim();
            if (value.equalsIgnoreCase("gb2312") || value.equalsIgnoreCase("gb18030")) {
                return "GBK";
            }
            return value;
        }
        return null;
    }

    private static boolean looksBrokenChinese(String text) {
        int replacement = 0;
        int sample = Math.min(text.length(), 4000);
        for (int i = 0; i < sample; i++) {
            if (text.charAt(i) == '\uFFFD') replacement++;
        }
        return replacement >= 3;
    }

    public boolean exists(String storageKey) {
        return Files.exists(resolveSafe(storageKey));
    }

    public long size(String storageKey) {
        try {
            return Files.size(resolveSafe(storageKey));
        } catch (IOException ex) {
            throw new BusinessException(500, "无法读取文件大小");
        }
    }

    public void deleteQuietly(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) return;
        try {
            Files.deleteIfExists(resolveSafe(storageKey));
        } catch (IOException ignored) {
            // Soft-deleted DB rows may keep orphan files for later cleanup.
        }
    }

    public String sha256(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        } catch (NoSuchAlgorithmException ex) {
            throw new BusinessException(500, "无法计算文件摘要");
        }
    }

    private Path resolveSafe(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            throw new BusinessException(400, "无效的存储标识");
        }
        String normalized = storageKey.replace('\\', '/');
        if (normalized.contains("..") || normalized.startsWith("/") || normalized.contains(":")) {
            throw new BusinessException(400, "非法存储路径");
        }
        Path resolved = root.resolve(normalized).normalize();
        if (!resolved.startsWith(root)) {
            throw new BusinessException(400, "非法存储路径");
        }
        return resolved;
    }

    private static String normalizeExtension(String extension) {
        String ext = extension == null ? "" : extension.trim().toLowerCase();
        if (ext.startsWith(".")) ext = ext.substring(1);
        if (!ext.equals("html") && !ext.equals("htm")) {
            throw new BusinessException(400, "仅支持 .html / .htm 文件");
        }
        return ext;
    }

    public record StoredObject(String storageKey, long size, String contentHash) {}
}
