package org.keycloak.logging;

import java.util.Collection;
import java.util.Collections;

import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;

import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

/**
 * MDC 提供者解析与清理工具类。
 * <p>在 {@code LOG_MDC} 特性未启用或会话不可用时回退到 {@link NoopMappedDiagnosticContextProvider}。</p>
 */
public final class MappedDiagnosticContextUtil {

    private static final Logger log = Logger.getLogger(MappedDiagnosticContextUtil.class);
    private static final MappedDiagnosticContextProvider NOOP_PROVIDER = new NoopMappedDiagnosticContextProvider();
    private static volatile Collection<String> keysToClear = Collections.emptySet();

    /** 获取当前会话的 MDC 提供者；特性关闭或不可用时返回空实现。 */
    public static MappedDiagnosticContextProvider getMappedDiagnosticContextProvider(KeycloakSession session) {
        if (!Profile.isFeatureEnabled(Profile.Feature.LOG_MDC)) {
            return NOOP_PROVIDER;
        }
        if (session == null) {
            log.warn("Cannot obtain session from thread to init MappedDiagnosticContextProvider. Return Noop provider.");
            return NOOP_PROVIDER;
        }
        MappedDiagnosticContextProvider provider = session.getProvider(MappedDiagnosticContextProvider.class);
        if (provider == null) {
            return NOOP_PROVIDER;
        }
        return provider;
    }

    /** 注册请求结束时需从 MDC 移除的键集合。 */
    public static void setKeysToClear(Collection<String> keys) {
        // 避免调用昂贵的 MDC.getMap()，改为遍历已知键列表清理
        keysToClear = keys;
    }

    /**
     * 清理 MDC，但仅移除本提供者写入的键值对。
     */
    public static void clearMdc() {
        for (String key : keysToClear) {
            MDC.remove(key);
        }
    }
}
