package org.keycloak.ssf.transmitter.delivery.poll;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.events.outbox.OutboxStore;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.jpa.entities.OutboxEntryEntity;
import org.keycloak.models.jpa.entities.OutboxEntryStatus;
import org.keycloak.ssf.transmitter.metrics.SsfMetricsBinder;
import org.keycloak.ssf.transmitter.outbox.SsfOutboxKinds;

import org.jboss.logging.Logger;

/**
 * 编排一次 RFC 8936 轮询请求：先 ack（避免已处理行出现在下一批），再读取调用接收方的
 * 下一批 {@code PENDING} POLL 行。
 *
 * <p>除 {@link KeycloakSession} 传递外无状态——JAX-RS 资源内每次请求新建实例。</p>
 */
public class PollDeliveryService {

    private static final Logger log = Logger.getLogger(PollDeliveryService.class);

    /** 接收方省略 {@code maxEvents} 时 RFC 8936 §2.1 的默认值。 */
    public static final int DEFAULT_MAX_EVENTS = 100;

    /**
     * 接收方 {@code maxEvents} 的硬上限，防止一次请求拉取整个积压。
     * 默认过紧时可后续配置；1000 远高于典型接收方批量（50–100）。
     */
    public static final int MAX_EVENTS_CAP = 1000;

    /**
     * 单次 {@code ack} 或 {@code setErrs} 批次中 jti 数量的硬上限。两者共用同一限制，
     * 使请求内存有界，且 per-(client, jti) IN 查询不超过 Oracle 1000 元素上限。
     * 超大批次 poll 端点返回 {@code 400 invalid_request}——接收方应拆成多次 poll。
     */
    public static final int MAX_BATCH_CAP = 1000;

    protected final KeycloakSession session;

    protected final OutboxStore outboxStore;

    protected final SsfMetricsBinder metricsBinder;

    public PollDeliveryService(KeycloakSession session, OutboxStore outboxStore, SsfMetricsBinder metricsBinder) {
        this.session = session;
        this.outboxStore = outboxStore;
        this.metricsBinder = metricsBinder;
    }

    /**
     * 执行 ack + 读取并返回响应体。调用方负责校验 {@code receiverClient} 拥有 URL 中的流——
     * 本服务在已授权客户端上操作。
     */
    public PollResponse poll(ClientModel receiverClient, PollRequest request) {

        int maxEvents = clampMaxEvents(request.getMaxEvents());
        String realmName = currentRealmName();
        String labelClientId = receiverClient.getClientId();

        // 1. Ack first. The receiver's natural pattern is "ack what I
        //    got last time, fetch the next batch", so processing the ack
        //    before the read prevents the same rows from coming back
        //    inside one request.
        List<String> ack = request.getAck();
        if (ack != null && !ack.isEmpty()) {
            outboxStore.ackPendingForOwner(SsfOutboxKinds.POLL, receiverClient.getId(), ack);
            metricsBinder.recordPollAck(realmName, labelClientId, ack.size());
        }

        // 2. Then NACK (setErrs). Receiver-reported errors transition
        //    matching PENDING POLL rows to DEAD_LETTER with the
        //    receiver's error message in last_error. Done after ack so
        //    the natural "ack the ones I processed, NACK the ones I
        //    couldn't" sequence works.
        Map<String, Map<String, Object>> setErrs = request.getSetErrs();
        Map<String, String> errorByJti = toErrorMessages(setErrs);
        if (!errorByJti.isEmpty()) {
            outboxStore.nackPendingForOwner(SsfOutboxKinds.POLL, receiverClient.getId(), errorByJti);
            metricsBinder.recordPollNack(realmName, labelClientId, errorByJti.size());
        }

        // 3. Read the next batch — UPGRADE_SKIPLOCKED so concurrent
        //    pollers (e.g. multiple receiver pods on the same OAuth
        //    credentials) walk disjoint rows.
        List<OutboxEntryEntity> rows = outboxStore.lockPendingForOwner(SsfOutboxKinds.POLL,
                receiverClient.getId(), maxEvents);
        metricsBinder.recordPollServed(realmName, labelClientId, rows.size());

        Map<String, String> sets = new LinkedHashMap<>(rows.size());
        for (OutboxEntryEntity row : rows) {
            sets.put(row.getCorrelationId(), row.getPayload());
        }

        // 4. moreAvailable: if we filled the batch we have to assume
        //    there's more (count(*) is a wasted query for a probably-
        //    yes answer); if we didn't fill the batch there can't be
        //    more available than what we just locked.
        boolean moreAvailable = false;
        if (rows.size() == maxEvents) {
            long pending = outboxStore.countForOwnerByStatus(SsfOutboxKinds.POLL,
                    receiverClient.getId(), OutboxEntryStatus.PENDING);
            moreAvailable = pending > rows.size();
        }

        if (log.isDebugEnabled()) {
            log.debugf("SSF poll. clientId=%s ackCount=%d nackCount=%d returnedCount=%d moreAvailable=%s",
                    receiverClient.getClientId(),
                    ack == null ? 0 : ack.size(),
                    errorByJti.size(),
                    sets.size(),
                    moreAvailable);
        }

        PollResponse response = new PollResponse();
        response.setSets(sets);
        response.setMoreAvailable(moreAvailable);
        return response;
    }

    /**
     * 将 wire 形态的 {@code setErrs}（RFC 8936 §2.1 每 jti 的 {@code err} + {@code description}）
     * 扁平化为 per-jti 错误消息字符串，写入发件箱 {@code last_error} 列。
     * 部分描述符（仅 err、仅 description 或皆无）尽力格式化——不因描述符畸形而拒绝请求。
     */
    protected Map<String, String> toErrorMessages(Map<String, Map<String, Object>> setErrs) {
        if (setErrs == null || setErrs.isEmpty()) {
            return Map.of();
        }
        Map<String, String> messages = new LinkedHashMap<>(setErrs.size());
        for (Map.Entry<String, Map<String, Object>> entry : setErrs.entrySet()) {
            messages.put(entry.getKey(), formatNackMessage(entry.getValue()));
        }
        return messages;
    }

    protected String formatNackMessage(Map<String, Object> descriptor) {
        if (descriptor == null) {
            return "<receiver NACK with no descriptor>";
        }
        Object err = descriptor.get("err");
        Object description = descriptor.get("description");
        StringBuilder sb = new StringBuilder("Receiver NACK");
        if (err != null) {
            sb.append(" err=").append(err);
        }
        if (description != null) {
            sb.append(" description=").append(description);
        }
        return sb.toString();
    }

    /**
     * 安全获取当前 realm <em>名称</em>——用作 {@code realm} 指标标签，
     * 使仪表板显示 {@code realm="ssf-poc"} 而非 opaque realm UUID。
     */
    protected String currentRealmName() {
        try {
            return session.getContext().getRealm().getName();
        } catch (RuntimeException e) {
            return null;
        }
    }

    protected int clampMaxEvents(Integer requested) {
        if (requested == null) {
            return DEFAULT_MAX_EVENTS;
        }
        if (requested < 1) {
            return 1;
        }
        if (requested > MAX_EVENTS_CAP) {
            return MAX_EVENTS_CAP;
        }
        return requested;
    }
}
