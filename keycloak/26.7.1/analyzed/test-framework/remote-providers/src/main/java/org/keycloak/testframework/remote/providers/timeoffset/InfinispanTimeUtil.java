package org.keycloak.testframework.remote.providers.timeoffset;

import java.io.Serializable;

import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.testframework.remote.providers.runonserver.RunOnServer;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;

import static org.keycloak.connections.infinispan.InfinispanUtil.setTimeServiceToKeycloakTime;

/**
 * 在服务器端切换 Infinispan 缓存时间服务的测试工具。
 * <p>
 * 应通过 {@link RunOnServer} 或 {@code @TestOnServer} 在 Keycloak 进程内调用，
 * 以便与 {@link TimeOffSetRealmResourceProvider} 配合模拟时间偏移。
 */
public class InfinispanTimeUtil implements Serializable {

    protected static final Logger logger = Logger.getLogger(InfinispanTimeUtil.class);

    private static Runnable origTimeService = null;

    /** @return 在服务器上启用 Keycloak 测试时间服务的 {@link RunOnServer} 任务 */
    public static RunOnServer enableTestingTimeService() {
        return InfinispanTimeUtil::enableTestingTimeService;
    }

    /** @return 在服务器上恢复原始 Infinispan 时间服务的 {@link RunOnServer} 任务 */
    public static RunOnServer disableTestingTimeService() {
        return InfinispanTimeUtil::disableTestingTimeService;
    }

    /**
     * 将 Infinispan 缓存管理器的时间服务替换为 Keycloak 可控实现。
     *
     * @param session 用于获取 {@link InfinispanConnectionProvider} 的会话
     */
    public static void enableTestingTimeService(KeycloakSession session) {
        if (origTimeService != null) {
            return;
        }

        InfinispanConnectionProvider ispnProvider = session.getProvider(InfinispanConnectionProvider.class);

        logger.info("Will set KeycloakIspnTimeService to the infinispan cacheManager");
        EmbeddedCacheManager cacheManager = ispnProvider.getCache(InfinispanConnectionProvider.USER_CACHE_NAME).getCacheManager();
        origTimeService = setTimeServiceToKeycloakTime(cacheManager);
    }

    /**
     * 恢复先前保存的 Infinispan 时间服务。
     *
     * @param session 当前 Keycloak 会话（未直接使用，保持 API 一致）
     * @throws IllegalStateException 若尚未启用测试时间服务
     */
    public static void disableTestingTimeService(KeycloakSession session) {
        if (origTimeService == null) {
            throw new IllegalStateException("Calling revertTimeService when testing TimeService was not set");
        }

        origTimeService.run();
        origTimeService = null;
    }

}
