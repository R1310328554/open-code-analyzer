package org.keycloak.ssf.event;

import org.keycloak.provider.Provider;

/**
 * 按会话暴露全局 {@link SsfEventRegistry} 的 Provider。
 * <p>注册表在服务器启动时由所有 {@link SsfEventProviderFactory} 的贡献聚合构建一次，
 * 查找开销低且无状态。</p>
 */
public interface SsfEventProvider extends Provider {

    @Override
    default void close() {
    }

    /**
     * @return 全局不可变的已知 SSF 事件注册表
     */
    SsfEventRegistry getRegistry();
}
