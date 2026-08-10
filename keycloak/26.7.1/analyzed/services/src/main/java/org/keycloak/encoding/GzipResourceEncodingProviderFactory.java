package org.keycloak.encoding;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.keycloak.Config;
import org.keycloak.common.Version;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.services.resources.KeycloakApplication;

import org.apache.commons.io.FileUtils;
import org.jboss.logging.Logger;

/**
 * Gzip 资源编码 SPI 工厂。
 * <p>创建 {@link GzipResourceEncodingProvider}，并维护版本化 Gzip 缓存目录；可通过 {@code excludedContentTypes} 排除无需压缩的 MIME 类型。</p>
 */
public class GzipResourceEncodingProviderFactory implements ResourceEncodingProviderFactory {

    private static final Logger logger = Logger.getLogger(GzipResourceEncodingProviderFactory.class);

    /** 不参与 Gzip 编码的 Content-Type 集合。 */
    private Set<String> excludedContentTypes = new HashSet<>();

    /** 懒初始化的 Gzip 缓存目录。 */
    private File cacheDir;

    @Override
    /** @param session 当前会话 @return Gzip 资源编码提供者 */
    public ResourceEncodingProvider create(KeycloakSession session) {
        if (cacheDir == null) {
            cacheDir = initCacheDir();
        }

        return new GzipResourceEncodingProvider(cacheDir);
    }

    @Override
    /** 从配置加载 {@code excludedContentTypes}（空格分隔）。 */
    public void init(Config.Scope config) {
        String e = config.get("excludedContentTypes", "image/png image/jpeg");
        excludedContentTypes.addAll(Arrays.asList(e.split(" ")));
    }

    @Override
    /** @param contentType 响应 Content-Type @return 不在排除列表中时返回 {@code true} */
    public boolean encodeContentType(String contentType) {
        return !excludedContentTypes.contains(contentType);
    }

    @Override
    /** @return SPI 工厂标识 {@code gzip} */
    public String getId() {
        return "gzip";
    }

    @Override
    /** @return 排除 Content-Type 列表的配置元数据 */
    public List<ProviderConfigProperty> getConfigMetadata() {
        return ProviderConfigurationBuilder.create()
                .property()
                .name("excludedContentTypes")
                .type("string")
                .helpText("A space separated list of content-types to exclude from encoding.")
                .defaultValue("image/png image/jpeg")
                .add()
                .build();
    }

    /** 初始化版本化 Gzip 缓存目录并清理旧版本缓存。 */
    private synchronized File initCacheDir() {
        if (cacheDir != null) {
            return cacheDir;
        }

        File cacheRoot = new File(KeycloakApplication.getTmpDirectory(), "kc-gzip-cache");
        File cacheDir = new File(cacheRoot, Version.RESOURCES_VERSION);

        if (cacheRoot.isDirectory()) {
            for (File f : cacheRoot.listFiles()) {
                if (!f.getName().equals(Version.RESOURCES_VERSION)) {
                    try {
                        FileUtils.deleteDirectory(f);
                    } catch (IOException e) {
                        logger.warn("Failed to delete old gzip cache directory", e);
                    }
                }
            }
        }

        cacheDir.mkdirs();
        if (!cacheDir.isDirectory()) {
            logger.warn("Failed to create gzip cache directory " + cacheDir.getAbsolutePath());
            return null;
        }

        return cacheDir;
    }
}
