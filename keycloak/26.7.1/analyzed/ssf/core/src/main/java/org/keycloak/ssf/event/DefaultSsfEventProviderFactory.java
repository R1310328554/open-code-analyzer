package org.keycloak.ssf.event;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.ssf.event.caep.CaepCredentialChange;
import org.keycloak.ssf.event.caep.CaepSessionRevoked;
import org.keycloak.ssf.event.stream.SsfStreamUpdatedEvent;
import org.keycloak.ssf.event.stream.SsfStreamVerificationEvent;

/**
 * 默认 {@link SsfEventProviderFactory}，贡献内置的标准 SSF / CAEP 事件集。
 * <p>该工厂还负责在 {@link #postInit(KeycloakSessionFactory)} 中
 * 将所有已注册工厂的贡献合并为全局 {@link SsfEventRegistry}。</p>
 */
public class DefaultSsfEventProviderFactory implements SsfEventProviderFactory, EnvironmentDependentProviderFactory {

    public static final String PROVIDER_ID = "default";

    /**
     * 标准事件贡献映射，键为事件类型 URI，值为 {@code ::new} 方法引用，
     * 使注册表可在运行时（如合成事件发射器）无反射地实例化新对象。
     * <p>使用 {@link LinkedHashMap} 保持插入顺序，便于管理 UI 等场景按序遍历。</p>
     */
    private static final Map<String, Supplier<? extends SsfEvent>> STANDARD_EVENT_FACTORIES;

    static {
        Map<String, Supplier<? extends SsfEvent>> events = new LinkedHashMap<>();

        // SSF Stream events
        events.put(SsfStreamVerificationEvent.TYPE, SsfStreamVerificationEvent::new);
        events.put(SsfStreamUpdatedEvent.TYPE, SsfStreamUpdatedEvent::new);

        // CAEP events
        events.put(CaepCredentialChange.TYPE, CaepCredentialChange::new);
        events.put(CaepSessionRevoked.TYPE, CaepSessionRevoked::new);

        STANDARD_EVENT_FACTORIES = Map.copyOf(events);
    }

    /**
     * {@link #STANDARD_EVENT_FACTORIES} 中发射端可实际发送的事件子集。
     * 可由 {@code SecurityEventTokenMapper} 从 Keycloak 事件生成，
     * 或由管理员通过 emit API 按需触发。
     * 映射中其余内置事件仅注册供接收端解析对应类型的 SET。
     * <p>此处两种类型对应 OpenID CAEP 互操作配置文件 1.0 的用例：
     * {@code session-revoked} 与 {@code credential-change}。</p>
     *
     * @see <a href="https://openid.github.io/sharedsignals/openid-caep-interoperability-profile-1_0.html">OpenID CAEP Interoperability Profile 1.0</a>
     */
    public static final Set<String> EMITTABLE_EVENT_TYPES = Set.of(
            CaepCredentialChange.TYPE,
            CaepSessionRevoked.TYPE);

    /**
     * {@link #EMITTABLE_EVENT_TYPES} 中 {@code SecurityEventTokenMapper}
     * 从 Keycloak 监听器事件原生生成的事件子集，驱动管理 UI 的「原生发射」标记。
     */
    public static final Set<String> NATIVELY_EMITTED_EVENT_TYPES = Set.of(
            CaepCredentialChange.TYPE,
            CaepSessionRevoked.TYPE);

    private volatile SsfEventRegistry registry;

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Map<String, Supplier<? extends SsfEvent>> getContributedEventFactories() {
        return STANDARD_EVENT_FACTORIES;
    }

    @Override
    public Set<String> getEmittableEventTypes() {
        return EMITTABLE_EVENT_TYPES;
    }

    @Override
    public Set<String> getNativelyEmittedEventTypes() {
        return NATIVELY_EMITTED_EVENT_TYPES;
    }

    @Override
    public SsfEventProvider create(KeycloakSession session) {
        return new DefaultSsfEventProvider(registry);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        this.registry = SsfEventProviderFactory.buildRegistry(factory);
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.SSF);
    }
}
