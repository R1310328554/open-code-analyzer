package org.keycloak.protocol.oauth2.cimd.clientpolicy.executor;

import org.keycloak.Config;
import org.keycloak.protocol.oauth2.cimd.provider.PersistentClientIdMetadataDocumentProviderFactory;

/**
 * {@link AbstractClientIdMetadataDocumentExecutorFactory} 的全局工厂 Provider 配置。
 * <p>封装 CIMD Provider 名称、元数据缓存时间上下限及元数据字节数上限等键值。</p>
 *
 * @author <a href="mailto:takashi.norimatsu.ws@hitachi.com">Takashi Norimatsu</a>
 */
public class ClientIdMetadataDocumentExecutorFactoryProviderConfig {

    private final Config.Scope config;

    /** CIMD Provider 标识名。 */

    private final String cimdProviderName;

    /** 元数据缓存最短有效期（秒）。 */

    private final int minCacheTime;

    /** 元数据缓存最长有效期（秒）。 */

    private final int maxCacheTime;

    /** 客户端元数据文档允许的最大字节数。 */

    private final long upperLimitMetadataBytes;

    /** {@link #cimdProviderName} 的默认值。 */

    public static final String DEFAULT_CONFIG_CIMD_PROVIDER_NAME = PersistentClientIdMetadataDocumentProviderFactory.PROVIDER_ID;

    /** {@link #minCacheTime} 的默认值：5 分钟。 */

    public static final int DEFAULT_CONFIG_MIN_CACHE_TIME = 300;

    /** {@link #maxCacheTime} 的默认值：30 天。 */

    public static final int DEFAULT_CONFIG_MAX_CACHE_TIME = 259200; // 30days

    /** {@link #upperLimitMetadataBytes} 的默认值：5KB。 */

    public static final long DEFAULT_CONFIG_UPPER_LIMIT_METADATA_BYTES = 5000;


    /**
     * 从 Keycloak 配置作用域读取 CIMD 执行器相关全局项。
     * @param config 配置作用域
     */
    public ClientIdMetadataDocumentExecutorFactoryProviderConfig(Config.Scope config) {
        this.config = config;
        cimdProviderName = config.get(AbstractClientIdMetadataDocumentExecutorFactory.CONFIG_CIMD_PROVIDER_NAME, DEFAULT_CONFIG_CIMD_PROVIDER_NAME);
        minCacheTime = config.getInt(AbstractClientIdMetadataDocumentExecutorFactory.CONFIG_MIN_CACHE_TIME, DEFAULT_CONFIG_MIN_CACHE_TIME);
        maxCacheTime = config.getInt(AbstractClientIdMetadataDocumentExecutorFactory.CONFIG_MAX_CACHE_TIME, DEFAULT_CONFIG_MAX_CACHE_TIME);
        upperLimitMetadataBytes = config.getLong(AbstractClientIdMetadataDocumentExecutorFactory.CONFIG_UPPER_LIMIT_METADATA_BYTES, DEFAULT_CONFIG_UPPER_LIMIT_METADATA_BYTES);
    }

    /** @return 配置的 CIMD Provider 名称 */
    public String getCimdProviderName() {
        return cimdProviderName;
    }

    /** @return 最小缓存时间（秒） */
    public int getMinCacheTime() {
        return minCacheTime;
    }

    /** @return 最大缓存时间（秒） */
    public int getMaxCacheTime() {
        return maxCacheTime;
    }

    /** @return 元数据文档字节数上限 */
    public long getUpperLimitMetadataBytes() {
        return upperLimitMetadataBytes;
    }
}
