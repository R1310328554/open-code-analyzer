package org.keycloak.ssf.event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.keycloak.models.KeycloakSession;
import org.keycloak.ssf.event.stream.SsfStreamUpdatedEvent;
import org.keycloak.ssf.event.stream.SsfStreamVerificationEvent;

/**
 * 所有已知 SSF 事件类型及其别名的不可变注册表。
 * <p>服务器启动时由所有 {@link SsfEventProviderFactory} 的贡献聚合构建一次，
 * 经 {@link SsfEventProvider#getRegistry()} 暴露。</p>
 */
public final class SsfEventRegistry {

    private final Map<String, Class<? extends SsfEvent>> classByEventType;

    private final Map<String, Class<? extends SsfEvent>> classByAlias;

    private final Map<String, String> aliasByEventType;

    private final Map<String, String> eventTypeByAlias;

    private final Map<String, Supplier<? extends SsfEvent>> factoryByEventType;

    private final Set<String> emittableEventTypes;

    private final Set<String> nativelyEmittedEventTypes;

    SsfEventRegistry(
            Map<String, Class<? extends SsfEvent>> classByEventType,
            Map<String, Class<? extends SsfEvent>> classByAlias,
            Map<String, String> aliasByEventType,
            Map<String, String> eventTypeByAlias,
            Map<String, Supplier<? extends SsfEvent>> factoryByEventType,
            Set<String> emittableEventTypes,
            Set<String> nativelyEmittedEventTypes) {
        this.classByEventType = Collections.unmodifiableMap(classByEventType);
        this.classByAlias = Collections.unmodifiableMap(classByAlias);
        this.aliasByEventType = Collections.unmodifiableMap(aliasByEventType);
        this.eventTypeByAlias = Collections.unmodifiableMap(eventTypeByAlias);
        this.factoryByEventType = Collections.unmodifiableMap(factoryByEventType);
        this.emittableEventTypes = Collections.unmodifiableSet(emittableEventTypes);
        this.nativelyEmittedEventTypes = Collections.unmodifiableSet(nativelyEmittedEventTypes);
    }

    /** 从给定工厂的贡献构建聚合注册表，供 {@link SsfEventProviderFactory#buildRegistry} 使用。 */
    static SsfEventRegistry from(Collection<? extends SsfEventProviderFactory> factories) {

        Map<String, Class<? extends SsfEvent>> classByEventType = new HashMap<>();
        Map<String, Class<? extends SsfEvent>> classByAlias = new HashMap<>();
        Map<String, String> aliasByEventType = new HashMap<>();
        Map<String, String> eventTypeByAlias = new HashMap<>();
        Map<String, Supplier<? extends SsfEvent>> factoryByEventType = new HashMap<>();
        Set<String> emittableEventTypes = new LinkedHashSet<>();
        Set<String> nativelyEmittedEventTypes = new LinkedHashSet<>();

        for (SsfEventProviderFactory factory : factories) {
            for (Map.Entry<String, Supplier<? extends SsfEvent>> entry
                    : factory.getContributedEventFactories().entrySet()) {
                String eventType = entry.getKey();
                Supplier<? extends SsfEvent> eventFactory = entry.getValue();

                // Instantiate once at registry-build time to derive the
                // event class (used by Jackson as the deserialization
                // target) and the alias (explicit override or fallback
                // to the class' simple name). The factory is stored
                // alongside so callers that need fresh instances at
                // runtime (e.g. the synthetic event emitter) can invoke
                // eventFactory.get() directly without reflection.
                SsfEvent sample = eventFactory.get();
                Class<? extends SsfEvent> eventClass = sample.getClass();
                String alias = sample.getAlias() != null ? sample.getAlias() : eventClass.getSimpleName();

                classByEventType.put(eventType, eventClass);
                classByAlias.put(alias, eventClass);
                aliasByEventType.put(eventType, alias);
                eventTypeByAlias.put(alias, eventType);
                factoryByEventType.put(eventType, eventFactory);
            }
            emittableEventTypes.addAll(factory.getEmittableEventTypes());
            nativelyEmittedEventTypes.addAll(factory.getNativelyEmittedEventTypes());
        }

        return new SsfEventRegistry(classByEventType, classByAlias, aliasByEventType,
                eventTypeByAlias, factoryByEventType, emittableEventTypes,
                nativelyEmittedEventTypes);
    }

    /**
     * 返回给定完整事件类型 URI 对应的 {@link SsfEvent} 类；
     * 未注册时返回空 {@link Optional}。
     */
    public Optional<Class<? extends SsfEvent>> getEventClassByType(String eventType) {
        return Optional.ofNullable(classByEventType.get(eventType));
    }

    /**
     * 返回给定事件类型 URI 的 {@link SsfEvent} 实例工厂，供合成事件发射器等无反射创建默认事件体。
     * 未注册时返回空 {@link Optional}。
     */
    public Optional<Supplier<? extends SsfEvent>> getEventFactoryByType(String eventType) {
        return Optional.ofNullable(factoryByEventType.get(eventType));
    }

    /** 按别名或完整事件类型 URI 解析 {@link SsfEvent} 类；均无匹配时返回 {@code null}。 */
    public Class<? extends SsfEvent> resolveEventClass(String aliasOrEventType) {
        Class<? extends SsfEvent> eventClass = classByAlias.get(aliasOrEventType);
        if (eventClass != null) {
            return eventClass;
        }
        return classByEventType.get(aliasOrEventType);
    }

    /** 解析给定完整事件类型 URI 的别名（如 {@code CaepCredentialChange}）；未注册时返回 {@code null}。 */
    public String resolveAliasForEventType(String eventType) {
        return aliasByEventType.get(eventType);
    }

    /** 解析给定别名对应的完整事件类型 URI；未注册时返回 {@code null}。 */
    public String resolveEventTypeForAlias(String alias) {
        return eventTypeByAlias.get(alias);
    }

    /** @return 所有已知事件别名（已排序） */
    public Set<String> getKnownAliases() {
        return Collections.unmodifiableSet(new TreeSet<>(eventTypeByAlias.keySet()));
    }

    /** @return 所有已知事件类型 URI */
    public Set<String> getKnownEventTypes() {
        return Collections.unmodifiableSet(classByEventType.keySet());
    }

    /**
     * 流内部生命周期事件类型（verification SET、stream-updated SET）。
     * 由发射端端到端拥有；外部调用方（合成 emit、管理 UI）不得伪造，
     * 否则可冒充发射端行为。合成 emit 门禁与管理 UI 的可用事件列表均过滤此类事件。
     */
    public static final Set<String> STREAM_LIFECYCLE_EVENT_TYPES = Set.of(
            SsfStreamVerificationEvent.TYPE,
            SsfStreamUpdatedEvent.TYPE);

    /**
     * 接收端可在 stream-create/update 的 {@code events_requested} 中合法请求的事件类型，
     * 即全注册表减去仅发射端可产生的 {@link #STREAM_LIFECYCLE_EVENT_TYPES}。
     * 驱动管理 UI「可用支持事件」多选，使操作员可配置接收任意可投递类型。
     */
    public Set<String> getReceiverRequestableEventTypes() {
        Set<String> known = classByEventType.keySet();
        if (known.isEmpty()) {
            return Set.of();
        }
        java.util.Set<String> result = new java.util.LinkedHashSet<>(known.size());
        for (String type : known) {
            if (!STREAM_LIFECYCLE_EVENT_TYPES.contains(type)) {
                result.add(type);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    /**
     * 返回 {@link #getReceiverRequestableEventTypes()} 中发射端可发送的子集，
     * 聚合各 {@link SsfEventProviderFactory#getEmittableEventTypes()}。
     * <p>驱动向接收端通告的默认 {@code events_supported} 及 emit API 白名单。</p>
     */
    public Set<String> getEmittableEventTypes() {
        return emittableEventTypes;
    }

    /**
     * 返回 {@link #getEmittableEventTypes()} 中由 Keycloak 监听器/触发器
     * 原生发射的子集，由各 {@link SsfEventProviderFactory#getNativelyEmittedEventTypes()} 声明。
     * <p><b>非强制门禁</b>，仅供管理 UI 展示「内置」标记，区分自动触发与仅 emit API 可发送的事件。</p>
     */
    public Set<String> getNativelyEmittedEventTypes() {
        return nativelyEmittedEventTypes;
    }

    public static Set<String> parseEventTypeAliases(String eventAliases) {
        return Set.copyOf(Stream.of(eventAliases.split(",")).map(String::trim).toList());
    }

    public static SsfEventRegistry of(KeycloakSession session) {
        SsfEventProvider eventsProvider = session.getProvider(SsfEventProvider.class);
        if (eventsProvider == null) {
            return null;
        }
        return eventsProvider.getRegistry();
    }
}
