package org.keycloak.client.admin.cli.v2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.keycloak.client.cli.util.OutputUtil;

/**
 * 按服务器 URL 缓存 v2 命令描述符的本地存储。
 * <p>
 * 使用 {@code registry.json} 映射服务器到 OpenAPI 版本，描述符文件名为 {@code descriptor-<version>.json}。
 */
public final class KcAdmV2DescriptorCache {

    /** 服务器→版本注册表文件名。 */
    public static final String REGISTRY_FILENAME = "registry.json";
    /** 描述符 JSON 文件前缀。 */
    public static final String DESCRIPTOR_PREFIX = "descriptor-";

    private final Path cacheDir;

    public KcAdmV2DescriptorCache(Path cacheDir) {
        this.cacheDir = cacheDir;
    }

    /** 加载指定服务器 URL 对应的缓存描述符；不存在或损坏时返回 {@code null}。 */
    public KcAdmV2CommandDescriptor loadForServer(String serverUrl) {
        if (!Files.isDirectory(cacheDir)) {
            return null;
        }
        Registry registry = readRegistry();
        if (registry == null) {
            return null;
        }
        ServerEntry entry = registry.servers.get(serverUrl);
        if (entry == null || entry.version == null) {
            return null;
        }
        Path descriptorFile = descriptorPath(entry.version);
        if (!Files.isRegularFile(descriptorFile)) {
            return null;
        }
        try {
            return OutputUtil.MAPPER.readValue(descriptorFile.toFile(), KcAdmV2CommandDescriptor.class);
        } catch (IOException e) {
            return null;
        }
    }

    /** 保存描述符并更新注册表；版本变更时清理无引用的旧描述符文件。 */
    public void save(String serverUrl, KcAdmV2CommandDescriptor descriptor) {
        String newVersion = descriptor.getVersion();
        if (newVersion == null || newVersion.isBlank()) {
            throw new IllegalArgumentException("Descriptor version must not be null or blank");
        }

        try {
            Files.createDirectories(cacheDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache directory: " + cacheDir, e);
        }

        Registry registry = readRegistryOrEmpty();
        String oldVersion = versionForServer(registry, serverUrl);

        try {
            OutputUtil.MAPPER.writeValue(descriptorPath(newVersion).toFile(), descriptor);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write descriptor: " + descriptorPath(newVersion), e);
        }

        registry.servers.put(serverUrl, new ServerEntry(newVersion));
        writeRegistry(registry);

        if (oldVersion != null && !oldVersion.equals(newVersion)) {
            deleteOrphanedDescriptor(registry, oldVersion);
        }
    }

    private void deleteOrphanedDescriptor(Registry registry, String version) {
        boolean stillReferenced = registry.servers.values().stream().anyMatch(e -> version.equals(e.version));
        if (!stillReferenced) {
            try {
                Files.deleteIfExists(descriptorPath(version));
            } catch (IOException ignored) {
            }
        }
    }

    private Path descriptorPath(String version) {
        String sanitized = version.replaceAll("[^a-zA-Z0-9_-]", "_");
        return cacheDir.resolve(DESCRIPTOR_PREFIX + sanitized + ".json");
    }

    private String versionForServer(Registry registry, String serverUrl) {
        ServerEntry entry = registry.servers.get(serverUrl);
        return entry != null ? entry.version : null;
    }

    private Registry readRegistry() {
        Path registryFile = cacheDir.resolve(REGISTRY_FILENAME);
        if (!Files.isRegularFile(registryFile)) {
            return null;
        }
        try {
            Registry registry = OutputUtil.MAPPER.readValue(registryFile.toFile(), Registry.class);
            if (registry == null || registry.servers == null) {
                return null;
            }
            return registry;
        } catch (IOException e) {
            return null;
        }
    }

    private Registry readRegistryOrEmpty() {
        Registry registry = readRegistry();
        return registry != null ? registry : new Registry();
    }

    private void writeRegistry(Registry registry) {
        try {
            OutputUtil.MAPPER.writeValue(cacheDir.resolve(REGISTRY_FILENAME).toFile(), registry);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write registry: " + cacheDir.resolve(REGISTRY_FILENAME), e);
        }
    }

    /** 服务器 URL → 描述符版本映射。 */
    static class Registry {
        public Map<String, ServerEntry> servers = new LinkedHashMap<>();
    }

    /** 单个服务器条目：缓存的描述符 OpenAPI 版本号。 */
    static class ServerEntry {
        public String version;

        ServerEntry() {}

        ServerEntry(String version) {
            this.version = version;
        }
    }
}
