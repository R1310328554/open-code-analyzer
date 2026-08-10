/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.api.ai.model.skills;

import com.alibaba.nacos.api.ai.model.NacosAiConfigKeyCodec;
import com.alibaba.nacos.api.utils.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Skill 操作工具类，提供 Markdown/ZIP 转换、本地同步与 Nacos Config 键构建等功能。
 *
 * @author nacos
 */
public class SkillUtils {
    
    private static final String EMPTY_STRING = "";
    
    private static final String METADATA_ENCODING = "encoding";
    
    private static final String METADATA_ENCODING_BASE64 = "base64";
    
    private static final String PATH_TRAVERSAL_SEQUENCE = "..";
    
    /** ZIP 本地文件头魔数：PK\x03\x04。 */
    private static final byte[] ZIP_MAGIC = {0x50, 0x4B, 0x03, 0x04};
    
    /** 合法 ZIP 的最小字节数（本地文件头为 30 字节）。 */
    private static final int ZIP_MIN_SIZE = 30;
    
    /** 本地同步时处理已存在 Skill 目录的策略。 */
    public enum ExistingDirectoryStrategy {
        /** 覆盖已有目录（删除后重建）。 */
        OVERWRITE,
        
        /** 将已有目录重命名为带时间戳后缀的备份目录。 */
        BACKUP,
        
        /** 目录已存在时抛出异常。 */
        FAIL
    }
    
    /**
     * 从 Skill 对象获取完整 SKILL.md Markdown 正文。
     *
     * @param skill 待转换的 Skill 对象
     * @return SKILL.md Markdown 正文
     */
    public static String toMarkdown(Skill skill) {
        if (skill == null) {
            return EMPTY_STRING;
        }
        
        return skill.getSkillMd() == null ? EMPTY_STRING : skill.getSkillMd();
    }
    
    /**
     * 将 Skill 对象转换为包含全部 Skill 文件的 ZIP 字节数组。
     *
     * <p>ZIP 目录结构与上传格式一致：
     * {@code skillName/SKILL.md}、{@code skillName/type/resourceName} 等。
     * 标记 metadata encoding=base64 的二进制资源会解码为原始字节。</p>
     *
     * @param skill 待转换的 Skill 对象
     * @return ZIP 文件字节数组
     * @throws IOException ZIP 创建失败时抛出
     * @throws IllegalArgumentException skill 为 null 或名称为空时抛出
     */
    public static byte[] toZipBytes(Skill skill) throws IOException {
        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }
        if (StringUtils.isBlank(skill.getName())) {
            throw new IllegalArgumentException("Skill name cannot be blank");
        }
        
        String skillName = skill.getName();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 1. 写入 SKILL.md
            zos.putNextEntry(new ZipEntry(skillName + "/SKILL.md"));
            zos.write(toMarkdown(skill).getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            
            // 2. 写入资源文件
            if (skill.getResource() != null && !skill.getResource().isEmpty()) {
                for (SkillResource resource : skill.getResource().values()) {
                    if (resource == null || StringUtils.isBlank(resource.getName())) {
                        continue;
                    }
                    String entryPath = buildZipEntryPath(skillName, resource);
                    zos.putNextEntry(new ZipEntry(entryPath));
                    byte[] bytes = resolveResourceBytes(resource);
                    zos.write(bytes);
                    zos.closeEntry();
                }
            }
        }
        return baos.toByteArray();
    }
    
    /**
     * 构建 Skill 资源在 ZIP 中的条目路径。
     *
     * @param skillName Skill 名称（根目录）
     * @param resource  Skill 资源
     * @return ZIP 条目路径，如 "skillName/type/resourceName" 或 "skillName/resourceName"
     */
    private static String buildZipEntryPath(String skillName, SkillResource resource) {
        String type = resource.getType();
        String entryPath;
        if (!StringUtils.isBlank(type)) {
            entryPath = skillName + "/" + type + "/" + resource.getName();
        } else {
            entryPath = skillName + "/" + resource.getName();
        }
        validatePathSafety(entryPath);
        return entryPath;
    }
    
    /**
     * 校验路径不含目录穿越序列或绝对路径指示符。
     *
     * @param path 待校验路径
     * @throws SecurityException 路径含不安全序列时抛出
     */
    public static void validatePathSafety(String path) {
        if (path == null) {
            return;
        }
        if (path.contains(PATH_TRAVERSAL_SEQUENCE)) {
            throw new SecurityException("Path traversal detected: " + path);
        }
        if (path.startsWith("/") || path.startsWith("\\")) {
            throw new SecurityException("Absolute path not allowed: " + path);
        }
    }
    
    /**
     * 校验解析后的目标路径仍在指定基目录内。
     *
     * @param baseDir 必须包含目标的基目录
     * @param target  解析后的目标路径
     * @throws SecurityException 目标路径逃逸出基目录时抛出
     */
    public static void validatePathContainment(Path baseDir, Path target) {
        if (!target.normalize().startsWith(baseDir.normalize())) {
            throw new SecurityException(
                "Path escapes target directory: " + target + " is outside " + baseDir);
        }
    }
    
    /**
     * 通过魔数头校验字节数组是否为合法 ZIP 文件。
     *
     * @param data 待校验字节数组
     * @throws IllegalArgumentException 数据为 null、过短或缺少 ZIP 魔数头时抛出
     */
    public static void validateZipBytes(byte[] data) {
        if (data == null || data.length < ZIP_MIN_SIZE) {
            throw new IllegalArgumentException(
                "Invalid ZIP data: too short (" + (data == null ? 0 : data.length) + " bytes)");
        }
        for (int i = 0; i < ZIP_MAGIC.length; i++) {
            if (data[i] != ZIP_MAGIC[i]) {
                throw new IllegalArgumentException(
                    "Invalid ZIP data: missing ZIP magic header (PK\\x03\\x04)");
            }
        }
    }
    
    /**
     * 校验 ZIP 内全部条目路径，防止目录穿越与绝对路径。
     *
     * <p>仅扫描条目名而不解压内容，开销小，适合客户端校验下载的 ZIP。</p>
     *
     * @param data 待校验 ZIP 字节数组
     * @throws SecurityException 任一条目含目录穿越或绝对路径时抛出
     * @throws IOException ZIP 无法读取时抛出
     */
    public static void validateZipEntryPaths(byte[] data) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                validatePathSafety(entry.getName());
            }
        }
    }
    
    /**
     * 将资源内容解析为原始字节。
     * <p>metadata encoding=base64 的二进制资源会 Base64 解码；文本资源按 UTF-8 编码返回。</p>
     *
     * @param resource Skill 资源
     * @return 资源内容的原始字节
     */
    private static byte[] resolveResourceBytes(SkillResource resource) {
        String content = resource.getContent();
        if (content == null) {
            return new byte[0];
        }
        if (isBase64Encoded(resource)) {
            return Base64.getDecoder().decode(content);
        }
        return content.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * 判断资源是否为 Base64 编码的二进制内容。
     *
     * @param resource Skill 资源
     * @return metadata 含 encoding=base64 时返回 true
     */
    private static boolean isBase64Encoded(SkillResource resource) {
        Map<String, Object> metadata = resource.getMetadata();
        return metadata != null && METADATA_ENCODING_BASE64.equals(metadata.get(METADATA_ENCODING));
    }
    
    /**
     * 将 Skill 对象同步到本地目录（默认 OVERWRITE 策略）。
     * <p>创建 Skill 目录结构、SKILL.md 与资源文件。</p>
     *
     * @param skill 待同步的 Skill 对象
     * @param baseDir Skill 目录所在的基路径
     * @throws IOException 文件操作失败时抛出
     * @throws IllegalArgumentException skill 为 null 或名称为空时抛出
     */
    public static void syncToLocal(Skill skill, String baseDir) throws IOException {
        syncToLocal(skill, baseDir, ExistingDirectoryStrategy.OVERWRITE);
    }
    
    /**
     * Sync Skill object to local directory with strategy.
     * Creates the skill directory structure, SKILL.md file, and resource files.
     * Uses atomic operation: creates temporary directory first, writes all files,
     * then renames to final directory to ensure integrity.
     *
     * @param skill the Skill object to sync
     * @param baseDir the base directory path where the skill directory will be created
     * @param strategy the strategy for handling existing directories
     * @throws IOException if file operations fail
     * @throws IllegalArgumentException if skill is null or skill name is blank
     * @throws FileAlreadyExistsException if directory exists and strategy is FAIL
      * <p>Nacos AI Skill 模型 API；详见上方说明。</p>
     */
    public static void syncToLocal(Skill skill, String baseDir, ExistingDirectoryStrategy strategy)
        throws IOException {
        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }
        
        if (StringUtils.isBlank(skill.getName())) {
            throw new IllegalArgumentException("Skill name cannot be blank");
        }
        
        if (StringUtils.isBlank(baseDir)) {
            throw new IllegalArgumentException("Base directory cannot be blank");
        }
        
        if (strategy == null) {
            strategy = ExistingDirectoryStrategy.OVERWRITE;
        }
        
        // Create skill directory path: {baseDir}/{skillName}
        Path basePath = Paths.get(baseDir);
        Path skillDir = basePath.resolve(skill.getName());
        
        // Delegate to core implementation
        syncToLocalCore(skill, skillDir, basePath, strategy);
    }
    
    /**
     * Sync Skill object to local directory with custom skill directory name.
     * Creates the skill directory structure, SKILL.md file, and resource files.
     * Uses OVERWRITE strategy by default.
     *
     * @param skill the Skill object to sync
     * @param baseDir the base directory path where the skill directory will be created
     * @param skillDirName the custom directory name for the skill (if null, uses skill name)
     * @throws IOException if file operations fail
     * @throws IllegalArgumentException if skill is null or baseDir is blank
      * <p>Nacos AI Skill 模型 API；详见上方说明。</p>
     */
    public static void syncToLocal(Skill skill, String baseDir, String skillDirName)
        throws IOException {
        syncToLocal(skill, baseDir, skillDirName, ExistingDirectoryStrategy.OVERWRITE);
    }
    
    /**
     * Sync Skill object to local directory with custom skill directory name and strategy.
     * Creates the skill directory structure, SKILL.md file, and resource files.
     * Uses atomic operation: creates temporary directory first, writes all files,
     * then renames to final directory to ensure integrity.
     *
     * @param skill the Skill object to sync
     * @param baseDir the base directory path where the skill directory will be created
     * @param skillDirName the custom directory name for the skill (if null, uses skill name)
     * @param strategy the strategy for handling existing directories
     * @throws IOException if file operations fail
     * @throws IllegalArgumentException if skill is null or baseDir is blank
     * @throws FileAlreadyExistsException if directory exists and strategy is FAIL
      * <p>Nacos AI Skill 模型 API；详见上方说明。</p>
     */
    public static void syncToLocal(Skill skill, String baseDir, String skillDirName,
        ExistingDirectoryStrategy strategy) throws IOException {
        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }
        
        if (StringUtils.isBlank(baseDir)) {
            throw new IllegalArgumentException("Base directory cannot be blank");
        }
        
        if (strategy == null) {
            strategy = ExistingDirectoryStrategy.OVERWRITE;
        }
        
        // Use custom directory name or fall back to skill name
        String dirName = !StringUtils.isBlank(skillDirName) ? skillDirName : skill.getName();
        if (StringUtils.isBlank(dirName)) {
            throw new IllegalArgumentException("Skill directory name cannot be blank");
        }
        
        // Create skill directory path: {baseDir}/{skillDirName}
        Path basePath = Paths.get(baseDir);
        Path skillDir = basePath.resolve(dirName);
        
        // Delegate to core implementation
        syncToLocalCore(skill, skillDir, basePath, strategy);
    }
    
    /**
     * Core implementation for syncing Skill to local directory.
     * This method contains the common logic for all syncToLocal variants.
     *
     * @param skill the Skill object to sync
     * @param skillDir the target skill directory path
     * @param basePath the base directory path
     * @param strategy the strategy for handling existing directories
     * @throws IOException if file operations fail
     * @throws FileAlreadyExistsException if directory exists and strategy is FAIL
      * <p>Nacos AI Skill 模型 API；详见上方说明。</p>
     */
    private static void syncToLocalCore(Skill skill, Path skillDir, Path basePath,
        ExistingDirectoryStrategy strategy) throws IOException {
        // Step 1: If strategy is FAIL, check if directory exists and throw exception immediately
        if (strategy == ExistingDirectoryStrategy.FAIL) {
            if (Files.exists(skillDir) && Files.isDirectory(skillDir)) {
                throw new FileAlreadyExistsException("Skill directory already exists: " + skillDir);
            }
        }
        
        // Step 2: Create temporary directory and write all files
        String dirName = skillDir.getFileName().toString();
        Path tempSkillDir = basePath.resolve(dirName + ".tmp." + System.currentTimeMillis());
        
        try {
            // Create temporary skill directory
            Files.createDirectories(tempSkillDir);
            
            // Write SKILL.md file
            String markdownContent = toMarkdown(skill);
            Path skillMdPath = tempSkillDir.resolve("SKILL.md");
            Files.write(skillMdPath, markdownContent.getBytes(StandardCharsets.UTF_8));
            
            // Write resource files
            if (skill.getResource() != null && !skill.getResource().isEmpty()) {
                for (Map.Entry<String, SkillResource> entry : skill.getResource().entrySet()) {
                    SkillResource resource = entry.getValue();
                    if (resource == null) {
                        continue;
                    }
                    
                    String resourceName = resource.getName();
                    if (StringUtils.isBlank(resourceName)) {
                        // Use key as resource name if name is blank
                        resourceName = entry.getKey();
                    }
                    
                    String resourceType = resource.getType();
                    String resourceContent = resource.getContent();
                    
                    // Determine resource file path
                    Path resourcePath;
                    if (!StringUtils.isBlank(resourceType)) {
                        // Resources with type: {tempSkillDir}/{type}/{resourceName}
                        Path typeDir = tempSkillDir.resolve(resourceType);
                        Files.createDirectories(typeDir);
                        resourcePath = typeDir.resolve(resourceName);
                    } else {
                        // Resources without type: {tempSkillDir}/{resourceName}
                        resourcePath = tempSkillDir.resolve(resourceName);
                    }
                    // Security: ensure resolved path does not escape the skill directory
                    validatePathContainment(tempSkillDir, resourcePath);
                    
                    // Write resource content (use empty string if content is null)
                    String content = resourceContent != null ? resourceContent : "";
                    Files.write(resourcePath, content.getBytes(StandardCharsets.UTF_8));
                }
            }
            
            // Step 3: All files written successfully, now handle final directory
            boolean oldDirExists = Files.exists(skillDir) && Files.isDirectory(skillDir);
            
            if (!oldDirExists) {
                // Old directory doesn't exist, directly rename temp directory to final directory
                Files.move(tempSkillDir, skillDir, StandardCopyOption.ATOMIC_MOVE);
            } else {
                // Old directory exists, need to backup first
                // Step 3.1: Rename old directory to backup directory
                Path backupDir = createBackupDirectoryPath(skillDir);
                Files.move(skillDir, backupDir, StandardCopyOption.ATOMIC_MOVE);
                
                // Step 3.2: Rename temp directory to final directory
                Files.move(tempSkillDir, skillDir, StandardCopyOption.ATOMIC_MOVE);
                
                // Step 3.3: Handle backup directory based on strategy
                if (strategy == ExistingDirectoryStrategy.OVERWRITE) {
                    // Delete backup directory
                    deleteDirectory(backupDir);
                }
                // If strategy is BACKUP, keep the backup directory (do nothing)
            }
            
        } catch (Exception e) {
            // Clean up temporary directory on failure
            if (Files.exists(tempSkillDir)) {
                try {
                    deleteDirectory(tempSkillDir);
                } catch (IOException cleanupException) {
                    // Log but don't throw - original exception is more important
                }
            }
            throw e;
        }
    }
    
    /**
     * Create backup directory path with timestamp suffix.
     * If backup directory already exists, append counter to ensure uniqueness.
     *
     * @param skillDir the skill directory path
     * @return backup directory path
      * <p>Nacos AI Skill 模型 API；详见上方说明。</p>
     */
    private static Path createBackupDirectoryPath(Path skillDir) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        Path backupDir = skillDir.getParent()
            .resolve(skillDir.getFileName().toString() + ".backup." + timestamp);
        
        // If backup directory already exists, append counter
        int counter = 1;
        Path finalBackupDir = backupDir;
        while (Files.exists(finalBackupDir)) {
            finalBackupDir = skillDir.getParent().resolve(
                skillDir.getFileName().toString() + ".backup." + timestamp + "." + counter);
            counter++;
        }
        
        return finalBackupDir;
    }
    
    /**
     * Recursively delete a directory and all its contents.
     *
     * @param directory the directory to delete
     * @throws IOException if deletion fails
      * <p>Nacos AI Skill 模型 API；详见上方说明。</p>
     */
    private static void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        
        // Delete files before directories
        Files.walk(directory)
            .sorted((a, b) -> b.compareTo(a))
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete: " + path, e);
                }
            });
    }
    
    /**
     * Skill 主配置 dataId。
     *
     * @deprecated 已弃用。manifest 请使用 {@link #SKILL_INDEX_DATA_ID}，内容请使用版本化资源文件。
     */
    @Deprecated
    public static final String SKILL_MAIN_DATA_ID = "skill.json";
    
    /** 资源配置 dataId 前缀。 */
    public static final String RESOURCE_DATA_ID_PREFIX = "resource_";
    
    /** 资源配置 dataId 后缀。 */
    public static final String RESOURCE_DATA_ID_SUFFIX = ".json";
    
    /** Skill 配置 group 前缀。 */
    public static final String SKILL_GROUP_PREFIX = "skill_";
    
    /**
     * 客户端缓存用的 Skill 索引配置 dataId。
     * <p>服务端在 group {@code skill_{name}} 下写入 manifest 配置，
     * 包含当前在线版本与文件列表。</p>
     */
    public static final String SKILL_INDEX_DATA_ID = "skill_index.json";
    
    private static final String DOUBLE_UNDERSCORE = "__";
    
    /**
     * 构建 Skill 的 Nacos Config group（无版本后缀）。
     *
     * @param skillName Skill 名称
     * @return 配置 group 字符串，如 "skill_myskill"
     */
    public static String buildSkillGroup(String skillName) {
        return SKILL_GROUP_PREFIX + NacosAiConfigKeyCodec.encodeManifestGroupNameSegment(skillName);
    }
    
    /**
     * 构建指定 Skill 版本的 Nacos Config group。
     *
     * @param skillName Skill 名称
     * @param version   版本字符串，如 "v1"
     * @return 配置 group 字符串，如 "skill_myskill__v1"
     */
    public static String buildSkillVersionGroup(String skillName, String version) {
        return SKILL_GROUP_PREFIX + NacosAiConfigKeyCodec.encodeVersionedGroupSegment(skillName)
            + DOUBLE_UNDERSCORE
            + NacosAiConfigKeyCodec.encodeVersionedGroupSegment(version);
    }
    
    /**
     * 将 Skill 的 Nacos Config {@code group} 解码为逻辑 Skill 名称与可选版本。
     *
     * @param group 物理 group，如 {@code skill_myagent} 或 {@code skill_name__v1}
     * @return 长度为 2 的数组 {@code [skillName, version]}；manifest group 时 version 为 {@code null}
     */
    public static String[] decodeSkillGroupToNameAndVersion(String group) {
        if (StringUtils.isBlank(group) || !group.startsWith(SKILL_GROUP_PREFIX)) {
            throw new IllegalArgumentException("Not a Skill config group: " + group);
        }
        String rest = group.substring(SKILL_GROUP_PREFIX.length());
        int idx = rest.lastIndexOf(DOUBLE_UNDERSCORE);
        if (idx < 0) {
            return new String[] {NacosAiConfigKeyCodec.decodeSegment(rest), null};
        }
        return new String[] {NacosAiConfigKeyCodec.decodeSegment(rest.substring(0, idx)),
            NacosAiConfigKeyCodec.decodeSegment(rest.substring(idx + DOUBLE_UNDERSCORE.length()))};
    }
    
    /**
     * 将资源名清理为可用于 Nacos Config group 的安全值。
     *
     * @param name 原始资源名（如 skill 名或 agentspec 名）
     * @return 可用于 Nacos 配置参数的安全值
     * @deprecated 请使用 {@link NacosAiConfigKeyCodec#encodeSegment(String)} 进行可逆编码
     */
    @Deprecated
    public static String sanitizeNameForGroup(String name) {
        return NacosAiConfigKeyCodec.encodeManifestGroupNameSegment(name);
    }
    
    private static final String FILE_EXTENSION_PATTERN = ".*\\.[a-zA-Z0-9]+$";
    
    /**
     * 根据资源类型与名称生成资源 ID。
     * <p>格式为 {type}_{resourcename}；若名称以 .xx 结尾，最后一个 . 转为 __。
     * type 中的斜杠编码为点，以保证 dataId（resource_{resourceId}.json）在 Nacos 中合法。</p>
     *
     * @param type 资源类型（可为 null 或空；可含 / 表示多级路径）
     * @param resourceName 资源名称
     * @return 可用于 config dataId 的资源 ID
     */
    public static String generateResourceId(String type, String resourceName) {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            return "";
        }
        
        // 若资源名以 .xx 结尾，将最后一个 . 转为 __
        String processedName = resourceName;
        if (resourceName.matches(FILE_EXTENSION_PATTERN)) {
            // Replace only the last dot before the extension
            int lastDotIndex = resourceName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                processedName = resourceName.substring(0, lastDotIndex) + DOUBLE_UNDERSCORE
                    + resourceName.substring(lastDotIndex + 1);
            }
        }
        
        if (type != null && !type.trim().isEmpty()) {
            // 将 / 编码为 .，使 dataId 不含斜杠（兼容 Nacos 配置键）
            String safeType = type.trim().replace("/", ".");
            return safeType + "_" + processedName;
        } else {
            return processedName;
        }
    }
}
