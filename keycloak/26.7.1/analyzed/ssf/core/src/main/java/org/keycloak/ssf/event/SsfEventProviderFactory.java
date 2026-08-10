package org.keycloak.ssf.event;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderFactory;

/**
 * {@link SsfEventProvider} 实现的工厂契约。
 * <p>每个已注册工厂通过 {@link #getContributedEventFactories()} 贡献
 * 事件类型 URI 到 {@link SsfEvent} 工厂的映射；启动时合并为单一 {@link SsfEventRegistry}，
 * 经 {@link SsfEventProvider#getRegistry()} 暴露。</p>
 * <p>注册自定义事件：将实现本接口的工厂放入
 * {@code META-INF/services/org.keycloak.protocol.ssf.event.SsfEventProviderFactory}
 * 并在 {@link #getContributedEventFactories()} 中返回额外事件。</p>
 */
public interface SsfEventProviderFactory extends ProviderFactory<SsfEventProvider> {

    /**
     * 返回本 Provider 贡献给全局事件注册表的事件类型 URI 到 {@link SsfEvent} 工厂的映射。
     * 启动时调用一次，在 {@link ProviderFactory#init(org.keycloak.Config.Scope)} 之后、
     * {@link ProviderFactory#postInit(KeycloakSessionFactory)} 之前。
     * <p>工厂（通常为 {@code SomeEvent::new} 方法引用）使注册表可无反射地在运行时创建新实例。</p>
     * <p>默认返回空映射。</p>
     */
    default Map<String, Supplier<? extends SsfEvent>> getContributedEventFactories() {
        return Map.of();
    }

    /**
     * 返回 {@link #getContributedEventFactories()} 中发射端可实际发送的事件子集。
     * 聚合所有工厂的贡献，驱动向接收端通告的 {@code events_supported} 及 emit API 白名单。
     * <p>仅用于接收端入站解析的事件不得在此返回。</p>
     * <p>默认返回空集合。</p>
     */
    default Set<String> getEmittableEventTypes() {
        return Set.of();
    }

    /**
     * 返回 {@link #getEmittableEventTypes()} 中由 Keycloak 监听器/触发器
     * 原生发射（非仅依赖管理员 API）的进一步子集；管理 UI 以「内置」标记展示。
     * <p>仅可通过 admin emit API 发送的事件不得在此返回。</p>
     * <p>默认回退为 {@link #getEmittableEventTypes()}。</p>
     */
    default Set<String> getNativelyEmittedEventTypes() {
        return getEmittableEventTypes();
    }

    @Override
    default void init(Config.Scope config) {
        // no-op
    }

    @Override
    default void close() {
        // no-op
    }

    /**
     * 多数通过本 SPI 注册的工厂仅向共享 {@link SsfEventRegistry} 贡献事件，
     * 不提供 per-session {@link SsfEventProvider}（调用方使用 id 为 {@code "default"} 的默认工厂）。
     * <p>默认返回 {@code null}，仅贡献事件的扩展只需实现 {@code getId()}、
     * {@code isSupported()} 与 {@code getContributedEventFactories()}。
     * 仅在完全替换默认 Provider 时覆盖。</p>
     */
    @Override
    default SsfEventProvider create(KeycloakSession session) {
        return null;
    }

    /**
     * 将所有已注册 {@link SsfEventProviderFactory} 的贡献聚合为单一不可变 {@link SsfEventRegistry}。
     */
    static SsfEventRegistry buildRegistry(KeycloakSessionFactory sessionFactory) {
        Collection<? extends SsfEventProviderFactory> factories = sessionFactory
                .getProviderFactoriesStream(SsfEventProvider.class)
                .map(SsfEventProviderFactory.class::cast)
                .toList();

        return SsfEventRegistry.from(factories);
    }
}
