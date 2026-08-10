package org.keycloak.ssf.transmitter.outbox;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.function.BiFunction;

import org.keycloak.events.outbox.OutboxDeliveryHandler;
import org.keycloak.events.outbox.OutboxDeliveryResult;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.OutboxEntryEntity;
import org.keycloak.ssf.event.token.SsfSecurityEventToken;
import org.keycloak.ssf.transmitter.SsfTransmitterContext;
import org.keycloak.ssf.transmitter.SsfTransmitterProvider;
import org.keycloak.ssf.transmitter.delivery.push.PushDeliveryOutcome;
import org.keycloak.ssf.transmitter.delivery.push.PushDeliveryService;
import org.keycloak.ssf.transmitter.metrics.SsfMetricsBinder;
import org.keycloak.ssf.transmitter.stream.StreamConfig;

import org.jboss.logging.Logger;

/**
 * 通用发件箱的 SSF push 处理器。drainer 按 {@code entryKind = "ssf-push"} 查找此处理器，
 * 对每个到期行调用 {@link #deliver(KeycloakSession, OutboxEntryEntity)}；
 * 本实现解析行所指向的 realm/client/stream，并将编码 SET 交给 {@link PushDeliveryService}。
 *
 * <p>解析→投递→分类行为：
 * <ul>
 *   <li>Realm/client/stream 已不存在 → {@link OutboxDeliveryOutcome#ORPHANED}。</li>
 *   <li>Push 成功 → {@link OutboxDeliveryOutcome#DELIVERED}。</li>
 *   <li>Push 失败 → {@link OutboxDeliveryOutcome#RETRY}；drainer 根据尝试次数决定 RETRY 或 DEAD_LETTER。</li>
 * </ul>
 *
 * <p>每种结果均上报 {@code keycloak.ssf.push.delivery} 计量——DELIVERED/RETRY/ORPHANED。
 * 当处理器可提前检测到尝试耗尽时也会在此递增 DEAD_LETTER 计数；drainer 升级路径确保行状态一致。</p>
 */
public class SsfPushDeliveryHandler implements OutboxDeliveryHandler {

    private static final Logger log = Logger.getLogger(SsfPushDeliveryHandler.class);

    protected final SsfTransmitterContext context;
    protected final BiFunction<KeycloakSession, SsfTransmitterContext, PushDeliveryService> pushDeliveryServiceFactory;
    protected final SsfMetricsBinder metricsBinder;

    public SsfPushDeliveryHandler(SsfTransmitterContext context,
                                  BiFunction<KeycloakSession, SsfTransmitterContext, PushDeliveryService> pushDeliveryServiceFactory,
                                  SsfMetricsBinder metricsBinder) {
        this.context = Objects.requireNonNull(context, "context");
        this.pushDeliveryServiceFactory = Objects.requireNonNull(pushDeliveryServiceFactory, "pushDeliveryServiceFactory");
        this.metricsBinder = metricsBinder == null ? SsfMetricsBinder.NOOP : metricsBinder;
    }

    @Override
    public String entryKind() {
        return SsfOutboxKinds.PUSH;
    }

    @Override
    public OutboxDeliveryResult deliver(KeycloakSession session, OutboxEntryEntity row) {
        Instant rowStart = Instant.now();

        RealmModel realm = session.realms().getRealm(row.getRealmId());
        if (realm == null) {
            log.warnf("SSF push handler: row references unknown realm — orphaning. id=%s realmId=%s correlationId=%s",
                    row.getId(), row.getRealmId(), row.getCorrelationId());
            metricsBinder.recordPushDelivery(row.getRealmId(), row.getOwnerId(),
                    SsfMetricsBinder.PushOutcome.ORPHANED, Duration.between(rowStart, Instant.now()));
            return OutboxDeliveryResult.orphaned("unknown realm: " + row.getRealmId());
        }
        String realmLabel = realm.getName();

        ClientModel receiverClient = realm.getClientById(row.getOwnerId());
        if (receiverClient == null) {
            log.warnf("SSF push handler: row references unknown client — orphaning. id=%s ownerId=%s correlationId=%s",
                    row.getId(), row.getOwnerId(), row.getCorrelationId());
            metricsBinder.recordPushDelivery(realmLabel, row.getOwnerId(),
                    SsfMetricsBinder.PushOutcome.ORPHANED, Duration.between(rowStart, Instant.now()));
            return OutboxDeliveryResult.orphaned("unknown client: " + row.getOwnerId());
        }

        SsfTransmitterProvider transmitter = session.getProvider(SsfTransmitterProvider.class);
        if (transmitter == null) {
            // 中途特性不可用——保留行待处理，下 tick 重试。理想情况是 RETRY 但不增加尝试次数，
            // 但 drainer 总会递增；特性被禁用场景足够罕见，浪费一次尝试可接受。
            log.warnf("SSF push handler: transmitter provider unavailable — retrying row %s next tick", row.getId());
            return OutboxDeliveryResult.retry("transmitter provider unavailable");
        }

        String expectedStreamId = row.getContainerId();
        StreamConfig stream = transmitter.streamStore().getStreamForClient(receiverClient);
        if (stream == null
                || (expectedStreamId != null && !expectedStreamId.equals(stream.getStreamId()))) {
            log.warnf("SSF push handler: row's stream is gone — orphaning. id=%s ownerId=%s pendingStreamId=%s currentStreamId=%s",
                    row.getId(), row.getOwnerId(), expectedStreamId,
                    stream == null ? "<none>" : stream.getStreamId());
            metricsBinder.recordPushDelivery(realmLabel, receiverClient.getClientId(),
                    SsfMetricsBinder.PushOutcome.ORPHANED, Duration.between(rowStart, Instant.now()));
            return OutboxDeliveryResult.orphaned(
                    stream == null ? "stream removed" : "stream replaced (current=" + stream.getStreamId() + ")");
        }

        PushDeliveryOutcome push = deliverEncoded(session, stream, row);
        if (push.delivered()) {
            log.debugf("SSF push handler delivered. id=%s ownerId=%s streamId=%s correlationId=%s attempts=%d",
                    row.getId(), row.getOwnerId(), stream.getStreamId(), row.getCorrelationId(), row.getAttempts() + 1);
            metricsBinder.recordPushDelivery(realmLabel, receiverClient.getClientId(),
                    SsfMetricsBinder.PushOutcome.DELIVERED, Duration.between(rowStart, Instant.now()));
            return OutboxDeliveryResult.delivered();
        }

        // Push 失败：drainer 将根据尝试预算计算 next_attempt_at 或 dead-letter。
        // 此处以 RETRY 语义上报指标；若 drainer 在耗尽时升级为 DEAD_LETTER，
        // 可由 drainer 在后续挂钩单独指标路径。
        String lastError = formatLastError(push);
        log.debugf("SSF push handler delivery failed. id=%s ownerId=%s streamId=%s correlationId=%s lastError=%s",
                row.getId(), row.getOwnerId(), stream.getStreamId(), row.getCorrelationId(), lastError);
        metricsBinder.recordPushDelivery(realmLabel, receiverClient.getClientId(),
                SsfMetricsBinder.PushOutcome.RETRY, Duration.between(rowStart, Instant.now()));
        return OutboxDeliveryResult.retry(lastError);
    }

    /**
     * 通过新建的 {@link PushDeliveryService} 投递行内存储的编码 SET。
     * {@code PushDeliveryService} 除捕获的 HTTP 客户端与发送方配置外无状态，按行构造成本低。
     * 最小桩 {@link SsfSecurityEventToken} 携带关联 ID（jti）以便 push 服务日志有用——
     * 线上实际载荷为行的 {@code payload}（已签名编码 SET）。
     *
     * <p>捕获任意 RuntimeException，使结构化失败路径为唯一出口——
     * 否则 drainer 自身的 catch-all 会抹掉 {@link PushDeliveryOutcome} 细节。</p>
     */
    protected PushDeliveryOutcome deliverEncoded(KeycloakSession session, StreamConfig stream, OutboxEntryEntity row) {
        PushDeliveryService push = pushDeliveryServiceFactory.apply(session, context);
        SsfSecurityEventToken stub = new SsfSecurityEventToken();
        stub.setJti(row.getCorrelationId());
        try {
            return push.deliverEvent(stream, stub, row.getPayload());
        } catch (RuntimeException e) {
            log.warnf(e, "SSF push handler: push threw. id=%s ownerId=%s correlationId=%s",
                    row.getId(), row.getOwnerId(), row.getCorrelationId());
            String endpointUrl = stream != null && stream.getDelivery() != null
                    ? stream.getDelivery().getEndpointUrl() : null;
            return PushDeliveryOutcome.transportFailure(e, endpointUrl);
        }
    }

    /**
     * 构建 {@code last_error} 摘要行。三种形态对应 {@link PushDeliveryOutcome}：
     * <ul>
     *   <li>HTTP 非 2xx：{@code "HTTP <status> <url>: <body excerpt>"}</li>
     *   <li>传输失败：{@code "<ExceptionClass> <url>: <message>"}</li>
     *   <li>无效流配置：{@code "InvalidStreamConfig: <reason>"}</li>
     * </ul>
     * <p>body/异常消息在 {@link #LAST_ERROR_DETAIL_MAX} 处截断，使列（{@code VARCHAR(2048)}）
     * 能容纳前缀+url+细节。存储层 {@code truncateError} 提供最终硬上限作为纵深防御。</p>
     */
    protected String formatLastError(PushDeliveryOutcome push) {
        if (push.status() != null) {
            String body = truncateDetail(push.responseBody());
            return "HTTP " + push.status() + " " + nullToEmpty(push.endpointUrl()) + ": " + nullToEmpty(body);
        }
        if (push.exceptionClass() != null) {
            String message = truncateDetail(push.exceptionMessage());
            String url = push.endpointUrl();
            String simpleClass = push.exceptionClass().contains(".")
                    ? push.exceptionClass().substring(push.exceptionClass().lastIndexOf('.') + 1)
                    : push.exceptionClass();
            if (url == null) {
                return simpleClass + ": " + nullToEmpty(message);
            }
            return simpleClass + " " + url + ": " + nullToEmpty(message);
        }
        return "delivery failed";
    }

    protected static final int LAST_ERROR_DETAIL_MAX = 1024;

    protected static String truncateDetail(String detail) {
        if (detail == null) {
            return null;
        }
        if (detail.length() <= LAST_ERROR_DETAIL_MAX) {
            return detail;
        }
        return detail.substring(0, LAST_ERROR_DETAIL_MAX) + "...";
    }

    protected static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

}
