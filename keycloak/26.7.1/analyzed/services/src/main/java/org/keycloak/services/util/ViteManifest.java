package org.keycloak.services.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.keycloak.util.JsonSerialization;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 解析 Vite 构建生成的 manifest 文件。
 * <p>manifest 将未哈希的资源文件名映射到带哈希的版本，用于正确渲染脚本、样式等静态资源链接。</p>
 *
 * @see <a href="https://vitejs.dev/guide/backend-integration.html">Vite documentation — Backend Integration</a>
 */
public class ViteManifest {
    /** manifest 文件相对路径 */
    public static final String MANIFEST_FILE_PATH = ".vite/manifest.json";
    /** Account Console Vite 开发服务器 URL 环境变量名 */
    public static final String ACCOUNT_VITE_URL = "KC_ACCOUNT_VITE_URL";
    /** Admin Console Vite 开发服务器 URL 环境变量名 */
    public static final String ADMIN_VITE_URL = "KC_ADMIN_VITE_URL";

    /** 解析后的 manifest 条目映射 */
    private final HashMap<String, Chunk> manifest;

    private ViteManifest(HashMap<String, Chunk> value) {
        this.manifest = value;
    }

    /** 从输入流解析 Vite manifest JSON。 */
    public static ViteManifest parseFromInputStream(InputStream input) throws IOException {
        final var typeRef = new TypeReference<HashMap<String, Chunk>>() {};
        final var value = JsonSerialization.readValue(input, typeRef);

        return new ViteManifest(value);
    }

    /** 返回 manifest 中标记为 entry 的首个 chunk。 */
    public Chunk getEntryChunk() {
        return manifest.values().stream()
                .filter(chunk -> chunk.isEntry().orElse(false))
                .findFirst()
                .orElseThrow();
    }
}
