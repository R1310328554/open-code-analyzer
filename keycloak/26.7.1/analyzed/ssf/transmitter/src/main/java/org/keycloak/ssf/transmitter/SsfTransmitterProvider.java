package org.keycloak.ssf.transmitter;

import java.util.Set;

import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.Provider;
import org.keycloak.ssf.event.SsfEventProviderFactory;
import org.keycloak.ssf.event.SsfEventRegistry;
import org.keycloak.ssf.transmitter.delivery.SecurityEventTokenDispatcher;
import org.keycloak.ssf.transmitter.delivery.poll.PollDeliveryService;
import org.keycloak.ssf.transmitter.delivery.push.PushDeliveryService;
import org.keycloak.ssf.transmitter.emit.EventEmitterService;
import org.keycloak.ssf.transmitter.event.SecurityEventTokenEncoder;
import org.keycloak.ssf.transmitter.event.SecurityEventTokenMapper;
import org.keycloak.ssf.transmitter.metadata.TransmitterMetadataService;
import org.keycloak.ssf.transmitter.metrics.SsfMetricsBinder;
import org.keycloak.ssf.transmitter.resources.SsfStreamManagementResource;
import org.keycloak.ssf.transmitter.resources.SsfStreamStatusResource;
import org.keycloak.ssf.transmitter.resources.SsfStreamVerificationResource;
import org.keycloak.ssf.transmitter.resources.SsfSubjectManagementResource;
import org.keycloak.ssf.transmitter.stream.StreamService;
import org.keycloak.ssf.transmitter.stream.StreamVerificationService;
import org.keycloak.ssf.transmitter.stream.storage.SsfStreamStore;
import org.keycloak.ssf.transmitter.subject.SsfSubjectInclusionResolver;
import org.keycloak.ssf.transmitter.subject.SubjectManagementService;
import org.keycloak.ssf.transmitter.support.SsfPushUrlValidator;

/**
 * SSF（Shared Signals Framework）发送方 {@link Provider}。
 *
 * <p>发送方负责生成安全事件令牌（SET）并通过已配置流向已注册 SSF 接收方投递。
 * 暴露流管理、流验证、事件映射与事件派发服务，以及实现 SSF 发送方 REST API 的 JAX-RS 子资源。</p>
 *
 * @see <a href="https://openid.github.io/sharedsignals/openid-sharedsignals-framework-1_0.html">OpenID Shared Signals Framework 1.0</a>
 */
public interface SsfTransmitterProvider extends Provider {

    default void close() {
    }

    /**
     * 返回处理流验证请求的服务。
     *
     * @return 流验证服务
     */
    StreamVerificationService verificationService();

    /**
     * 返回管理发送方元数据的服务（支持事件、投递方式、配置端点等）。
     *
     * @return 发送方元数据服务
     */
    TransmitterMetadataService metadataService();

    /**
     * 返回将 Keycloak 事件（用户/管理事件）映射为 SSF SET 的映射器。
     *
     * @return 安全事件令牌映射器
     */
    SecurityEventTokenMapper securityEventTokenMapper();

    /**
     * 返回按各流投递配置向适用流投递 SET 的派发器。
     *
     * @return 安全事件令牌派发器
     */
    SecurityEventTokenDispatcher securityEventTokenDispatcher();

    /**
     * 返回用于签名出站 SET 的 JWS 编码器。
     * 按 session 缓存——组合服务构建器经 provider 复用与 dispatcher 相同的编码器实例。
     */
    SecurityEventTokenEncoder securityEventTokenEncoder();

    /**
     * 返回 HTTP push 投递服务。与 {@link #securityEventTokenEncoder} 相同原因按 session 缓存。
     */
    PushDeliveryService pushDeliveryService();

    /**
     * 返回 SSF 框架内管理主体（用户、客户端等）的服务。
     *
     * @return 主体管理服务
     */
    SubjectManagementService subjectManagementService();

    /**
     * Returns the read-side gate that decides whether a user / org
     * counts as a subscribed subject for a given receiver. Drives the
     * dispatcher's subject-selection filter and the synthetic-emit
     * dispatchability check. Default implementation reads the
     * {@code ssf.notify.<receiverClientId>} attribute via
     * {@link org.keycloak.ssf.transmitter.subject.SsfNotifyAttributes};
     * extensions plug additional inclusion sources (group attributes,
     * roles, external policy services) by overriding either this
     * accessor on a custom {@link SsfTransmitterProvider} subclass or
     * {@link SsfTransmitterServiceBuilder#createSubjectInclusionResolver}
     * on a custom service builder.
     */
    SsfSubjectInclusionResolver subjectInclusionResolver();

    /**
     * Returns the service that pushes synthetic SSF events injected by
     * a trusted IAM management client through the normal dispatch
     * pipeline. Backs the {@code /admin/realms/{realm}/ssf/clients/{id}/events/emit}
     * admin endpoint.
     */
    EventEmitterService eventEmitterService();

    /**
     * 返回流 CRUD（创建/读取/更新/删除）的 JAX-RS 子资源。
     *
     * @return 流管理端点
     */
    SsfStreamManagementResource streamManagementResource();

    /**
     * 返回存储与读取 SSF 流配置的服务。
     *
     * @return SSF 流存储服务
     */
    SsfStreamStore streamStore();

    /**
     * 返回管理 SSF 流（创建/更新/删除/查找）的服务。
     *
     * @return 流服务
     */
    StreamService streamService();

    /**
     * 返回处理 POLL 拉取请求的服务。
     * @return POLL 投递服务
     */
    PollDeliveryService pollDeliveryService();

    /**
     * 返回查询与更新流状态的 JAX-RS 子资源。
     *
     * @return 流状态端点
     */
    SsfStreamStatusResource streamStatusResource();

    /**
     * 返回触发流验证的 JAX-RS 子资源。
     *
     * @return 流验证端点
     */
    SsfStreamVerificationResource streamVerificationResource();

    /**
     * 返回主体管理（添加/移除主体）的 JAX-RS 子资源。
     *
     * @return 主体管理端点
     */
    SsfSubjectManagementResource subjectManagementResource();

    /**
     * 默认支持的事件集合。
     * @return 事件类型 URI 集合
     */
    Set<String> getDefaultSupportedEvents();

    /**
     * 将完整事件类型 URI 解析为别名（如 {@code CaepCredentialChange}）。
     * 未知类型返回 {@code null}，调用方可回退原 URI。
     *
     * <p>默认实现委托全局 {@link SsfEventRegistry}（由各 {@link SsfEventProviderFactory} 填充），
     * 扩展可添加自定义事件类型与别名而无需子类化发送方。</p>
     *
     * @param eventType 完整事件类型 URI
     * @return 匹配别名，未知时为 {@code null}
     */
    String resolveAliasForEventType(String eventType);

    /**
     * Aliases of every event type a receiver can legitimately
     * <em>request</em> via {@code events_requested} on stream-create —
     * the full registry minus the protocol-internal lifecycle events
     * (verification SET, stream-updated SET) that only the transmitter
     * may produce. Drives the admin UI's "available supported events"
     * multi-select. Includes event types Keycloak doesn't fire from
     * native event listeners but that an external system may emit
     * via the synthetic emit endpoint or a custom mapper.
     */
    Set<String> getAvailableEventAliases();

    /**
     * Aliases of the subset of {@link #getAvailableEventAliases()}
     * that some registered {@link SsfEventProviderFactory} declares
     * as <em>natively emitted</em> (i.e. the events Keycloak fires
     * automatically from native event listeners — credential change,
     * session revoked, etc.). Used by the admin UI as a "natively
     * emitted" badge, not as a delivery gate. Synthetic emit can fire
     * any event in {@link #getAvailableEventAliases()}, including
     * ones outside this set.
     */
    Set<String> getNativelyEmittedEventAliases();

    /**
     * 返回来源于 {@link SsfTransmitterProviderFactory} SPI 的不可变发送方全局配置快照。
     * 消费者应通过此方法读取有效默认 push 超时与发送方发起验证延迟。
     */
    SsfTransmitterConfig getConfig();

    /**
     * 返回 SSF 发送方共享 Prometheus 指标绑定器。
     * 未直接持有 binder 的热路径（尤其按请求构造的 poll 端点）经此访问器解析。
     * 永不为 null——指标禁用时返回 {@link SsfMetricsBinder#NOOP}。
     */
    SsfMetricsBinder metrics();

    /**
     * 返回本 provider 实例绑定的 {@link KeycloakSession}。
     * 在接口上暴露，使组合服务构建器（如 {@link SsfTransmitterServiceBuilder#createDispatcher}）
     * 无需向下转型即可将 session 注入所构造的服务。
     */
    KeycloakSession session();

    /**
     * 返回各 per-session provider 共享的工厂级 {@link SsfTransmitterContext}。
     * 承载长生命周期配置、指标绑定器、待发事件存储工厂与发行方 URL 工厂。
     */
    SsfTransmitterContext context();

    /**
     * 返回接收方所供 push URL 的共享 SSRF 防护门。
     * 由 {@link SsfTransmitterContext} 持有，各 per-session provider 复用同一实例；
     * 自定义 SPI 可通过覆盖 {@link SsfTransmitterServiceBuilder#createPushUrlValidator} 替换校验器。
     */
    default SsfPushUrlValidator pushUrlValidator() {
        return context().pushUrlValidator();
    }
}
