package org.keycloak.ssf.transmitter.delivery;

import java.util.Set;
import java.util.function.Function;

import org.keycloak.events.outbox.OutboxStore;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.ssf.Ssf;
import org.keycloak.ssf.SsfProfile;
import org.keycloak.ssf.event.SsfEventRegistry;
import org.keycloak.ssf.event.token.SecurityEventToken;
import org.keycloak.ssf.event.token.SseCaepSecurityEventToken;
import org.keycloak.ssf.event.token.SsfSecurityEventToken;
import org.keycloak.ssf.stream.DeliveryMethod;
import org.keycloak.ssf.stream.StreamStatusValue;
import org.keycloak.ssf.transmitter.SsfTransmitterConfig;
import org.keycloak.ssf.transmitter.delivery.push.PushDeliveryService;
import org.keycloak.ssf.transmitter.event.SecurityEventTokenEncoder;
import org.keycloak.ssf.transmitter.event.SsfSignatureAlgorithms;
import org.keycloak.ssf.transmitter.metrics.SsfMetricsBinder;
import org.keycloak.ssf.transmitter.outbox.SsfOutboxKinds;
import org.keycloak.ssf.transmitter.stream.StreamConfig;
import org.keycloak.ssf.transmitter.subject.SsfSubjectInclusionResolver;
import org.keycloak.ssf.transmitter.subject.SubjectSubscriptionFilter;

import org.jboss.logging.Logger;

/**
 * 安全事件令牌（SET）派发器：按流状态、事件类型与主体订阅过滤，将 SET 写入发件箱或直接推送。
 * <p>支持 PUSH 与 POLL 两种投递方式，并处理暂停流的 HELD 暂存。</p>
 */
public class SecurityEventTokenDispatcher {

    private static final Logger log = Logger.getLogger(SecurityEventTokenDispatcher.class);

    /**
     * 无发送方配置时的回退过滤器（例如遗留/测试子类直接构造派发器）。
     * 构建时禁用 §9.3 宽限期——工厂路径在 {@link #createSubjectSubscriptionFilter()} 中
     * 替换为感知配置的实例。
     */
    protected static final SubjectSubscriptionFilter DEFAULT_SUBJECT_SUBSCRIPTION_FILTER = new SubjectSubscriptionFilter();

    protected final KeycloakSession session;

    protected final SecurityEventTokenEncoder securityEventTokenEncoder;

    protected final PushDeliveryService pushDeliveryService;

    protected final SsfTransmitterConfig transmitterConfig;

    protected final Function<KeycloakSession, OutboxStore> outboxStoreFactory;

    protected final SubjectSubscriptionFilter subjectSubscriptionFilter;

    protected final SsfMetricsBinder metricsBinder;

    protected final SsfSubjectInclusionResolver subjectInclusionResolver;

    public SecurityEventTokenDispatcher(KeycloakSession session,
                                        SecurityEventTokenEncoder securityEventTokenEncoder,
                                        PushDeliveryService pushDeliveryService,
                                        SsfTransmitterConfig transmitterConfig,
                                        Function<KeycloakSession, OutboxStore> outboxStoreFactory) {
        this(session, securityEventTokenEncoder, pushDeliveryService, transmitterConfig, outboxStoreFactory,
                SsfMetricsBinder.NOOP, null);
    }

    public SecurityEventTokenDispatcher(KeycloakSession session,
                                        SecurityEventTokenEncoder securityEventTokenEncoder,
                                        PushDeliveryService pushDeliveryService,
                                        SsfTransmitterConfig transmitterConfig,
                                        Function<KeycloakSession, OutboxStore> outboxStoreFactory,
                                        SsfMetricsBinder metricsBinder) {
        this(session, securityEventTokenEncoder, pushDeliveryService, transmitterConfig, outboxStoreFactory,
                metricsBinder, null);
    }

    public SecurityEventTokenDispatcher(KeycloakSession session,
                                        SecurityEventTokenEncoder securityEventTokenEncoder,
                                        PushDeliveryService pushDeliveryService,
                                        SsfTransmitterConfig transmitterConfig,
                                        Function<KeycloakSession, OutboxStore> outboxStoreFactory,
                                        SsfMetricsBinder metricsBinder,
                                        SsfSubjectInclusionResolver subjectInclusionResolver) {
        this.session = session;
        this.securityEventTokenEncoder = securityEventTokenEncoder;
        this.pushDeliveryService = pushDeliveryService;
        this.transmitterConfig = transmitterConfig;
        this.outboxStoreFactory = outboxStoreFactory;
        this.subjectInclusionResolver = subjectInclusionResolver;
        this.subjectSubscriptionFilter = createSubjectSubscriptionFilter();
        this.metricsBinder = metricsBinder == null ? SsfMetricsBinder.NOOP : metricsBinder;
    }

    protected SubjectSubscriptionFilter createSubjectSubscriptionFilter() {
        if (transmitterConfig == null) {
            return DEFAULT_SUBJECT_SUBSCRIPTION_FILTER;
        }
        return new SubjectSubscriptionFilter(transmitterConfig.getSubjectRemovalGraceSeconds(),
                subjectInclusionResolver);
    }

    /**
     * 从安全事件令牌获取事件类型。
     *
     * @param eventToken 安全事件令牌
     * @return 事件类型，未找到时返回 null
     */
    protected String getEventType(SsfSecurityEventToken eventToken) {
        if (eventToken.getEvents() != null && !eventToken.getEvents().isEmpty()) {
            return eventToken.getEvents().keySet().iterator().next();
        }
        return null;
    }

    public void dispatchEvent(SsfSecurityEventToken eventToken, StreamConfig stream) {

        if (eventToken == null || stream == null) {
            return;
        }

        StreamStatusValue status = stream.getStatus();

        if (status == StreamStatusValue.disabled) {
            // SSF: disabled streams drop events outright.
            log.debugf("Dropping event for disabled stream. clientId=%s streamId=%s jti=%s",
                    stream.getClientClientId(), stream.getStreamId(), eventToken.getJti());
            metricsBinder.recordSuppressed(currentRealmName(), stream.getClientClientId(),
                    SsfMetricsBinder.SuppressReason.STATUS_DISABLED);
            return;
        }

        // Check if the stream is interested in this event type
        if (!isEventRequestedByStream(eventToken, stream)) {
            log.debugf("Skipping event delivery for stream because of unsupported event. clientId=%s streamId=%s jti=%s events=%s",
                    stream.getClientClientId(), stream.getStreamId(), eventToken.getJti(), eventToken.getEvents());
            metricsBinder.recordSuppressed(currentRealmName(), stream.getClientClientId(),
                    SsfMetricsBinder.SuppressReason.EVENT_NOT_REQUESTED);
            return;
        }

        if (!shouldDispatchForSubject(eventToken, stream)) {
            metricsBinder.recordSuppressed(currentRealmName(), stream.getClientClientId(),
                    SsfMetricsBinder.SuppressReason.SUBJECT_GATE);
            return;
        }

        if (status == StreamStatusValue.paused) {
            // SSF: paused streams hold events; they're released when
            // the stream is resumed (status returns to enabled).
            metricsBinder.recordSuppressed(currentRealmName(), stream.getClientClientId(),
                    SsfMetricsBinder.SuppressReason.STATUS_PAUSED_HELD);
            holdEvent(eventToken, stream);
            return;
        }

        deliverEvent(eventToken, stream);
    }

    /**
     * 安全获取当前 realm <em>名称</em>——用作 {@code realm} 指标标签，
     * 使操作员看到 {@code realm="ssf-poc"} 而非 opaque 的 realm UUID。
     * 派发时会话通常带 realm 上下文，但仍防护异常调用路径污染指标标签。
     */
    protected String currentRealmName() {
        try {
            return session.getContext().getRealm().getName();
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * 将完整事件类型 URI 解析为接收方友好的别名（如 {@code CaepCredentialChange}）。
     * 未注册时回退为 URI——使未知/自定义事件类型在指标侧仍可观测且不丢失计数。
     */
    protected String resolveEventAlias(String eventType) {
        if (eventType == null) {
            return null;
        }
        String alias = Ssf.events().getRegistry().resolveAliasForEventType(eventType);
        return alias != null ? alias : eventType;
    }

    /**
     * 解析写入发件箱行 {@code entry_type} 列的值。别名是 SSF 子系统各处的通用形式
     * （管理 UI、客户端属性、指标标签、管理员创建的流配置 {@code events_delivered}），
     * 存储别名使发件箱列与过滤集合一致——尤其
     * {@link org.keycloak.ssf.transmitter.stream.StreamService#evictPendingEventsOutsideDeliveredSet
     * evictPendingEventsOutsideDeliveredSet}。
     *
     * <p>无注册别名的自定义事件回退为 URI；畸形令牌回退为 {@code <unknown>}（列 NOT NULL，必须写入值）。</p>
     */
    protected String resolveEntryType(SsfSecurityEventToken eventToken) {
        String eventType = getEventType(eventToken);
        if (eventType == null) {
            return "<unknown>";
        }
        return resolveEventAlias(eventType);
    }

    /**
     * 主体订阅过滤。根据流的 {@code default_subjects} 设置及事件主体上
     * {@code ssf.notify.<clientId>} 属性是否存在，判断事件是否应投递到该流。
     *
     * <p>protected 以便子类覆盖过滤逻辑——例如自定义主体解析或对特定事件类型无条件跳过检查。</p>
     */
    protected boolean shouldDispatchForSubject(SsfSecurityEventToken eventToken, StreamConfig stream) {
        return subjectSubscriptionFilter.shouldDispatch(eventToken, stream, stream.getClientClientId(), session);
    }

    /**
     * mapper 运行前可调用的预令牌主体门控。若用户在当前订阅状态下可在该流接收<em>任意</em>事件则返回 {@code true}。
     * 使事件监听器在调用 {@code toSecurityEventToken} 前短路未订阅主体的流。
     * {@link #dispatchEvent} 内仍会执行完整基于令牌的门控，故 {@code event.getUserId()} 与最终令牌主体
     * （复合主体、模拟登录）不一致时仍安全。
     */
    public boolean shouldDispatchForUser(UserModel user, StreamConfig stream) {
        return subjectSubscriptionFilter.shouldDispatchForUser(user, stream, stream.getClientClientId(), session);
    }

    /**
     * 若事件令牌类型属于流的 {@code events_requested} 集合（接收方希望投递该事件）则返回 {@code true}。
     * 任一侧缺失时 fail-open（令牌无事件类型信息，或流未收窄订阅）。
     *
     * <p>比较前将每条 {@code events_requested} 规范化为 URI，因集合可能含不同形式：
     * <ul>
     *   <li>管理 UI 创建的流存别名（UI 使用的形式）。</li>
     *   <li>接收方创建的流存 URI（符合 SSF/CAEP 规范）。</li>
     * </ul>
     * 无此规范化时，管理 UI 收窄的流将无法匹配入站事件，派发会被全部抑制。</p>
     */
    protected boolean isEventRequestedByStream(SsfSecurityEventToken eventToken, StreamConfig stream) {
        String eventTypeUri = getEventType(eventToken);
        Set<String> eventsRequested = stream.getEventsRequested();
        if (eventTypeUri == null || eventsRequested == null) {
            return true;
        }
        SsfEventRegistry registry = Ssf.events().getRegistry();
        for (String requested : eventsRequested) {
            if (requested == null) {
                continue;
            }
            String canonical = registry.resolveEventTypeForAlias(requested);
            if (canonical == null) {
                // Either it's already a URI (resolveEventTypeForAlias
                // only knows aliases) or a custom unregistered string;
                // in both cases use the literal for comparison.
                canonical = requested;
            }
            if (eventTypeUri.equals(canonical)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isStreamEnabled(StreamConfig stream) {
        StreamStatusValue status = stream.getStatus();
        if (status == null) {
            // some SSF receivers don't provide a status
            return true;
        }
        return StreamStatusValue.enabled == status;
    }

    /**
     * 为暂停流暂存事件：签名后写入发件箱
     * {@link org.keycloak.models.jpa.entities.OutboxEntryStatus#HELD HELD} 状态。
     * drainer / POLL 端点跳过 HELD 行；流恢复时释放为 {@code PENDING}
     * （{@link org.keycloak.events.outbox.OutboxStore#releaseHeldForOwner releaseHeldForOwner}）。
     */
    protected void holdEvent(SsfSecurityEventToken eventToken, StreamConfig stream) {
        var delivery = stream.getDelivery();
        DeliveryMethod deliveryMethod = DeliveryMethod.valueOfUri(delivery.getMethod());

        try {
            SecurityEventToken narrowedEventToken = getNarrowedEventToken(eventToken, stream);
            String signatureAlgorithm = SsfSignatureAlgorithms.resolveForStream(stream, transmitterConfig);
            String encodedEvent = securityEventTokenEncoder.encode(narrowedEventToken, signatureAlgorithm);

            String realmId = session.getContext().getRealm().getId();
            String clientId = stream.getClientId();
            String streamId = stream.getStreamId();
            String jti = eventToken.getJti();
            String entryType = resolveEntryType(eventToken);

            OutboxStore outboxStore = outboxStoreFactory.apply(session);
            switch (deliveryMethod) {
                case PUSH, RISC_PUSH ->
                        outboxStore.enqueueHeld(SsfOutboxKinds.PUSH, realmId, clientId, streamId, jti, entryType, encodedEvent, null);
                case POLL, RISC_POLL ->
                        outboxStore.enqueueHeld(SsfOutboxKinds.POLL, realmId, clientId, streamId, jti, entryType, encodedEvent, null);
            }

            // HELD is a distinct outcome from normal delivery — we
            // already counted it under SuppressReason.STATUS_PAUSED_HELD
            // on the dispatch gate. No additional enqueued counter bump
            // here so the two signals ("would have fired but paused"
            // vs "actually enqueued for wire delivery") stay separable.

            log.debugf("Held event for paused stream. clientId=%s streamId=%s jti=%s deliveryMethod=%s",
                    stream.getClientClientId(), streamId, jti, deliveryMethod.name());
        } catch (Exception e) {
            log.errorf(e, "Error holding event for paused stream. clientId=%s streamId=%s",
                    stream.getClientClientId(), stream.getStreamId());
        }
    }

    /**
     * 异步投递：将已签名 SET 入队至持久化 {@link OutboxStore push outbox}。
     * 集群感知 drainer 在下一 tick 取行并推送到接收方端点，指数退避重试，
     * 超出配置尝试次数后进入死信。
     *
     * <p>入队即返回——接收方确认由 drainer 异步观测，非本调用方。用于事件监听器处理用户/管理 SET。</p>
     *
     * <p>验证 SET 需内联获知接收方接受/拒绝结果，请改用
     * {@link #deliverEventSync(SsfSecurityEventToken, StreamConfig)}，绕过发件箱同步推送。</p>
     */
    public void deliverEvent(SsfSecurityEventToken eventToken, StreamConfig stream) {
        deliverEventInternal(eventToken, stream, true);
    }

    /**
     * 同步投递事件并返回结果。在调用方线程执行——用于验证路径，
     * 使管理端「Verify」按钮与接收方发起的 {@code POST /streams/verify} 能报告真实成败，而非仅「已调度」。
     *
     * @return 接收方接受推送时为 {@code true}，任何投递错误时为 {@code false}
     */
    public boolean deliverEventSync(SsfSecurityEventToken eventToken, StreamConfig stream) {
        return deliverEventInternal(eventToken, stream, false);
    }

    private boolean deliverEventInternal(SsfSecurityEventToken eventToken, StreamConfig stream, boolean async) {
        var delivery = stream.getDelivery();
        if (delivery == null) {
            // A stream can legitimately exist without a delivery config —
            // e.g. a KEYCLOAK-managed stream created from the admin console
            // before the receiver has registered its push/poll endpoint.
            // There is nowhere to send the SET, so skip delivery instead of
            // NPEing on delivery.getMethod().
            log.warnf("No delivery configured for stream; skipping delivery. clientId=%s streamId=%s jti=%s",
                    stream.getClientClientId(), stream.getStreamId(), eventToken.getJti());
            return false;
        }
        String deliveryMethodUri = delivery.getMethod();
        DeliveryMethod deliveryMethod = DeliveryMethod.valueOfUri(deliveryMethodUri);

        switch (deliveryMethod) {
            case PUSH, RISC_PUSH -> {
                try {
                    SecurityEventToken narrowedEventToken = getNarrowedEventToken(eventToken, stream);

                    String signatureAlgorithm = SsfSignatureAlgorithms.resolveForStream(stream, transmitterConfig);
                    String encodedEvent = securityEventTokenEncoder.encode(narrowedEventToken, signatureAlgorithm);

                    log.debugf("Delivering event to stream via %s. clientId=%s streamId=%s jti=%s async=%s",
                            deliveryMethod.name(), stream.getClientClientId(), stream.getStreamId(), eventToken.getJti(), async);

                    if (async) {
                        // Outbox row records the full, unnarrowed eventToken:
                        // the stored encodedEvent is already the signed,
                        // stream-narrowed payload that will go on the wire
                        // verbatim at drainer time, so the row-level fields
                        // (jti, eventType) are only used for logging,
                        // dedup, and per-event bookkeeping — indexed on the
                        // unnarrowed token so retries and admin queries see
                        // stable identifiers independent of what the
                        // narrowing step happened to strip for a given
                        // stream profile.
                        deliverEventAsync(stream, eventToken, encodedEvent);
                        return true;
                    }
                    return pushDeliveryService.deliverEvent(stream, narrowedEventToken, encodedEvent).delivered();
                } catch (Exception e) {
                    log.errorf(e, "Error delivering event via PUSH to stream. clientId=%s streamId=%s"
                            ,stream.getClientClientId(), stream.getStreamId());
                    return false;
                }
            }
            case POLL, RISC_POLL -> {
                try {
                    // Poll path mirrors the async PUSH branch: narrow per
                    // stream profile, sign once, persist the encoded SET
                    // into the outbox. The poll endpoint reads + acks
                    // these rows on the receiver's schedule — there is no
                    // drainer that pushes them out. The `async` flag is
                    // ignored: poll has no synchronous-delivery option
                    // because there is nobody to push to until the
                    // receiver shows up.
                    SecurityEventToken narrowedEventToken = getNarrowedEventToken(eventToken, stream);

                    String signatureAlgorithm = SsfSignatureAlgorithms.resolveForStream(stream, transmitterConfig);
                    String encodedEvent = securityEventTokenEncoder.encode(narrowedEventToken, signatureAlgorithm);

                    log.debugf("Enqueuing event for poll delivery. clientId=%s streamId=%s jti=%s"
                            ,stream.getClientClientId(), stream.getStreamId(), eventToken.getJti());

                    enqueueForPoll(stream, eventToken, encodedEvent);
                    return true;
                } catch (Exception e) {
                    log.errorf(e, "Error enqueuing event for POLL delivery to stream. clientId=%s streamId=%s"
                            ,stream.getClientClientId(), stream.getStreamId());
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 将已签名 SET 持久化到 push 发件箱。行提交后集群 drainer 在下一 tick 取行并有限重试派发——
     * 本方法本身不发起 HTTP。
     *
     * <p>流上的 {@code clientId} 由
     * {@link org.keycloak.ssf.transmitter.stream.storage.client.ClientStreamStore#extractStreamConfig
     * ClientStreamStore.extractStreamConfig} 填充，派发器收到的流配置上始终有值。</p>
     */
    protected void deliverEventAsync(StreamConfig stream,
                                     SsfSecurityEventToken eventToken,
                                     String encodedEvent) {
        String realmId = session.getContext().getRealm().getId();
        String clientId = stream.getClientId();
        String streamId = stream.getStreamId();
        String jti = eventToken.getJti();
        String entryType = resolveEntryType(eventToken);

        OutboxStore outboxStore = outboxStoreFactory.apply(session);
        outboxStore.enqueuePending(SsfOutboxKinds.PUSH, realmId, clientId, streamId, jti, entryType, encodedEvent, null);
        metricsBinder.recordEnqueued(currentRealmName(), stream.getClientClientId(), "PUSH", entryType);
    }

    /**
     * 将已签名 SET 持久化到标记为 poll 投递的发件箱。接收方通过 poll 端点
     * （{@code POST /ssf/transmitter/receivers/{clientId}/streams/{stream_id}/poll}）拉取并 ack；
     * 无 drainer 任务处理 POLL 行。
     */
    protected void enqueueForPoll(StreamConfig stream,
                                  SsfSecurityEventToken eventToken,
                                  String encodedEvent) {
        String realmId = session.getContext().getRealm().getId();
        String clientId = stream.getClientId();
        String streamId = stream.getStreamId();
        String jti = eventToken.getJti();
        String entryType = resolveEntryType(eventToken);

        OutboxStore outboxStore = outboxStoreFactory.apply(session);
        outboxStore.enqueuePending(SsfOutboxKinds.POLL, realmId, clientId, streamId, jti, entryType, encodedEvent, null);
        metricsBinder.recordEnqueued(currentRealmName(), stream.getClientClientId(), "POLL", entryType);
    }

    protected SecurityEventToken getNarrowedEventToken(SsfSecurityEventToken eventToken, StreamConfig stream) {
        // StreamConfig.getProfile() is an SsfProfile enum — comparing it against
        // the String constants in Ssf (Ssf.PROFILE_SSF_1_0 etc.) always returned
        // false, so neither branch ever fired and the SSE CAEP converter was
        // silently skipped for Apple Business Manager. Compare the enum
        // directly instead.
        SsfProfile profile = stream.getProfile();
        if (profile == null) {
            profile = SsfProfile.SSF_1_0;
        }

        return switch (profile) {
            case SSF_1_0 -> narrowSsfEventToken(eventToken);
            // if legacy CAEP SSE profile is requested convert the event to old format
            // this is currently required for compatibility with apple business manager
            case SSE_CAEP -> narrowCaepSseEventToken(eventToken);
        };
    }

    /**
     * 将 {@link SsfSecurityEventToken} 收窄为更通用的 {@link SecurityEventToken}。
     *
     * @param eventToken 待收窄的安全事件令牌
     * @return 收窄后的安全事件令牌
     */
    protected SecurityEventToken narrowSsfEventToken(SsfSecurityEventToken eventToken) {
        return eventToken;
    }

    /**
     * 将 {@link SsfSecurityEventToken} 转换为更窄的 {@link SseCaepSecurityEventToken}。
     * 通过 {@link SseCaepEventConverter} 按共享信号与事件（SSE）标准变换令牌，
     * 用于与旧版实现（如 Apple Business Manager）兼容。
     *
     * @param eventToken 待转换的安全事件令牌，不可为 null
     * @return 转换后的 {@link SseCaepSecurityEventToken} 实例
     */
    protected SseCaepSecurityEventToken narrowCaepSseEventToken(SsfSecurityEventToken eventToken) {
        return SseCaepEventConverter.convert(eventToken);
    }
}
