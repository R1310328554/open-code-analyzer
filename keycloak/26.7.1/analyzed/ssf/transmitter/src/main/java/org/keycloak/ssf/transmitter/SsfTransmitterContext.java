package org.keycloak.ssf.transmitter;

import java.util.Set;
import java.util.function.Function;

import org.keycloak.events.outbox.OutboxStore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.ssf.transmitter.metrics.SsfMetricsBinder;
import org.keycloak.ssf.transmitter.support.SsfPushUrlValidator;

/**
 * SSF 发送方的工厂级上下文包：持有不依赖 {@link KeycloakSession} 的长生命周期配置与共享协作对象。
 * 在 SPI {@code init()} 时创建一次，供所有 per-session {@link SsfTransmitterProvider} 实例复用。
 *
 * <p>刻意<em>不</em>持有 {@code KeycloakSession} 引用——session 按请求生命周期，
 * 在此捆绑会导致生命周期歧义与 use-after-close 风险。per-session 服务在 provider 上懒加载；
 * {@link SsfTransmitterProvider#session() session()} 是 session 的唯一规范存放点。</p>
 */
public final class SsfTransmitterContext {

    private final SsfTransmitterConfig config;
    private final Set<String> defaultSupportedEventAliases;
    private final SsfMetricsBinder metricsBinder;
    private final Function<KeycloakSession, OutboxStore> outboxStoreFactory;
    private final Function<KeycloakSession, String> issuerUrlFactory;
    private final SsfTransmitterServiceBuilder services;
    private final SsfPushUrlValidator pushUrlValidator;

    public SsfTransmitterContext(SsfTransmitterConfig config,
                                 Set<String> defaultSupportedEventAliases,
                                 SsfMetricsBinder metricsBinder,
                                 Function<KeycloakSession, OutboxStore> outboxStoreFactory,
                                 Function<KeycloakSession, String> issuerUrlFactory,
                                 SsfTransmitterServiceBuilder services) {
        this.config = config;
        this.defaultSupportedEventAliases = defaultSupportedEventAliases;
        this.metricsBinder = metricsBinder == null ? SsfMetricsBinder.NOOP : metricsBinder;
        this.outboxStoreFactory = outboxStoreFactory;
        this.issuerUrlFactory = issuerUrlFactory;
        this.services = services;
        // Stateless and config-driven — build once at factory init via
        // the service builder so custom SPI deployments can plug in a
        // different validator implementation. Shared across every
        // per-session provider instance.
        this.pushUrlValidator = services.createPushUrlValidator(config);
    }

    public SsfTransmitterConfig config() {
        return config;
    }

    /**
     * 发送方向未自行配置列表的接收方所宣传的「默认支持事件」别名（或完整 URI）。
     * {@code null} 表示回退到注册表中所有已知事件类型。
     */
    public Set<String> defaultSupportedEventAliases() {
        return defaultSupportedEventAliases;
    }

    public SsfMetricsBinder metrics() {
        return metricsBinder;
    }

    /**
     * 为给定 session 解析 {@link OutboxStore}。
     * 间接层便于测试子类注入自定义 store 而无需覆盖整个 context。
     */
    public OutboxStore outboxStore(KeycloakSession session) {
        return outboxStoreFactory.apply(session);
    }

    /**
     * 函数引用变体——传给需要 {@code Function<KeycloakSession, OutboxStore>} 的构造器
     *（如 dispatcher、poll 服务与 stream 服务）。
     */
    public Function<KeycloakSession, OutboxStore> outboxStoreFactory() {
        return outboxStoreFactory;
    }

    /**
     * 解析给定 session 的 realm 级发行方 URL（{@code iss} 声明）。
     * 集中于此，使元数据服务与 SET 映射器共享同一来源。
     */
    public String issuerUrl(KeycloakSession session) {
        return issuerUrlFactory.apply(session);
    }

    public Function<KeycloakSession, String> issuerUrlFactory() {
        return issuerUrlFactory;
    }

    public SsfTransmitterServiceBuilder services() {
        return services;
    }

    /**
     * 接收方所供 push URL 的共享 SSRF 防护门。无状态且由配置驱动——
     * 在工厂 init 时绑定 {@link SsfTransmitterConfig#isAllowInsecurePushTargets()}，
     * 供所有 per-session provider 实例复用。
     */
    public SsfPushUrlValidator pushUrlValidator() {
        return pushUrlValidator;
    }
}
