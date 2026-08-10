package org.keycloak.ssf.transmitter.metrics;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.keycloak.common.util.Time;
import org.keycloak.models.jpa.entities.OutboxEntryStatus;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.BaseUnits;
import org.jboss.logging.Logger;

/**
 * SSF 发送方 Prometheus 指标的门面类。所有热路径（分发器、drainer、轮询端点）
 * 的遥测均经由此类，使计量器查找与基数策略集中在一处。
 *
 * <p>遵循 Keycloak 现有 Micrometer 约定，使用 {@link Metrics#globalRegistry 全局注册表}
 *（参见 {@code MicrometerUserEventMetricsEventListenerProviderFactory}）。
 * 即使 Quarkus Prometheus 端点被禁用，计数器与计时器开销也很低——写入空收集器。
 *
 * <h3>基数策略</h3>
 * <ul>
 *     <li>计数器/计时器按 {@code realm} + {@code client_id} 打标签。
 *         每个 realm 通常仅 1–5 个 SSF 接收方客户端，按客户端切片成本低，
 *         便于运维判断「哪个下游在抖动」。</li>
 *     <li>发件箱深度仪表仅按 {@code realm} + {@code status} 打标签。
 *         按客户端的仪表会在大规模部署中爆炸基数，管理 UI 已按需提供按客户端深度。</li>
 *     <li>drainer tick 计数器<em>不</em>按节点 ID 打标签：集群聚合速率回答
 *         「SSF 是否在别处 draining」，避免 Kubernetes Pod 名变更污染序列。</li>
 * </ul>
 *
 * <h3>深度仪表（缓存）</h3>
 * 若将发件箱深度绑定为普通仪表，每次抓取都需昂贵的 {@code COUNT(*)}。
 * drainer 每 tick 调用一次 {@link #updateOutboxDepthSnapshot(Map)} 写入分组聚合结果；
 * 仪表从内存快照读取，抓取零 DB 开销。深度因此最多滞后一个 drainer tick——
 * 对「积压增长」告警足够。
 *
 * <h3>空操作回退</h3>
 * 当 {@link SsfTransmitterConfig#isMetricsEnabled()} 为 false（或运行时缺少 Micrometer）时，
 * 工厂构造 {@link #NOOP} 而非真实绑定器。各方法变为可分支预测的空操作——
 * 热路径可无条件调用绑定器，无需 null 检查链。
 */
public class SsfMetricsBinder {

    private static final Logger log = Logger.getLogger(SsfMetricsBinder.class);

    private static final String PREFIX = "keycloak.ssf.";

    // 计数器 --------------------------------------------------------------
    public static final String METER_EVENTS_ENQUEUED = PREFIX + "events.enqueued";
    public static final String METER_EVENTS_SUPPRESSED = PREFIX + "events.suppressed";
    public static final String METER_PUSH_DELIVERY = PREFIX + "push.delivery";
    public static final String METER_POLL_SERVED = PREFIX + "poll.served";
    public static final String METER_POLL_ACK = PREFIX + "poll.ack";
    public static final String METER_POLL_NACK = PREFIX + "poll.nack";
    public static final String METER_DRAINER_TICK = PREFIX + "drainer.tick";
    public static final String METER_VERIFICATION_REQUESTS = PREFIX + "verification.requests";

    // 计时器 ----------------------------------------------------------------
    public static final String METER_PUSH_DELIVERY_DURATION = PREFIX + "push.delivery.duration";
    public static final String METER_DRAINER_TICK_DURATION = PREFIX + "drainer.tick.duration";
    public static final String METER_VERIFICATION_DURATION = PREFIX + "verification.duration";

    // 仪表 ----------------------------------------------------------------
    public static final String METER_OUTBOX_DEPTH = PREFIX + "outbox.depth";

    /**
     * 最近一次 drainer tick 尝试的 epoch 秒时间戳。
     * 运维可对 {@code time() - keycloak_ssf_drainer_tick_last_at_seconds > 120} 告警以检测停滞 drainer——
     * 与 {@link #METER_DRAINER_TICK}（基于计数器速率）互补，单条仪表查询给出「多久以前」。
     * 首次 tick 前暴露为 {@code 0}，避免新启动服务器被误判为立即停滞——
     * 告警规则应忽略直至至少观测到一次非零值。
     */
    public static final String METER_DRAINER_TICK_LAST_AT = PREFIX + "drainer.tick.last_at_seconds";

    /**
     * 分发器抑制结果分类，用作 suppressed 计数器的 {@code reason} 标签。
     * 字符串值稳定，便于 Prometheus 告警规则匹配。
     */
    public enum SuppressReason {
        STATUS_DISABLED("status_disabled"),
        STATUS_PAUSED_HELD("status_paused_held"),
        EVENT_NOT_REQUESTED("event_not_requested"),
        SUBJECT_GATE("subject_gate");

        private final String label;

        SuppressReason(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    /**
     * 单条待发发件箱行的 drainer 推送结果分类。
     */
    public enum PushOutcome {
        DELIVERED("delivered"),
        RETRY("retry"),
        DEAD_LETTER("dead_letter"),
        ORPHANED("orphaned");

        private final String label;

        PushOutcome(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public enum DrainerOutcome {
        OK("ok"),
        ERROR("error");

        private final String label;

        DrainerOutcome(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    /**
     * 触发验证分发的来源。便于按入口点切片 {@code verification.requests}，
     * 区分 {@code initiator="receiver"}（过度轮询）、
     * {@code initiator="transmitter"}（创建后自动触发）与
     * {@code initiator="admin"}（UI/REST）。
     */
    public enum VerificationInitiator {
        RECEIVER("receiver"),
        ADMIN("admin"),
        TRANSMITTER("transmitter");

        private final String label;

        VerificationInitiator(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    /**
     * 验证请求结果：
     * <ul>
     *     <li>{@code delivered} — 接收方接受了验证 SET。</li>
     *     <li>{@code failed} — 同步推送到接收方失败
     *         （网络错误、非 2xx，或接收方侧流查找为空）。</li>
     *     <li>{@code rate_limited} — 因接收方 {@code min_verification_interval}
     *         尚未到期而以 429 拒绝。仅在接收方发起路径触发。</li>
     * </ul>
     */
    public enum VerificationOutcome {
        DELIVERED("delivered"),
        FAILED("failed"),
        RATE_LIMITED("rate_limited");

        private final String label;

        VerificationOutcome(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    /**
     * 指标禁用或 Micrometer 不可用时的 NOOP 绑定器。所有方法均为空操作，
     * 包括快照更新——热路径调用方可无条件调用绑定器，无需 null 检查或分支。
     */
    public static final SsfMetricsBinder NOOP = new SsfMetricsBinder(true) {
        @Override
        public void recordEnqueued(String realmId, String clientId, String deliveryMethod, String eventType) {
        }

        @Override
        public void recordSuppressed(String realmId, String clientId, SuppressReason reason) {
        }

        @Override
        public void recordPushDelivery(String realmId, String clientId, PushOutcome outcome, Duration took) {
        }

        @Override
        public void recordPollServed(String realmId, String clientId, long count) {
        }

        @Override
        public void recordPollAck(String realmId, String clientId, long count) {
        }

        @Override
        public void recordPollNack(String realmId, String clientId, long count) {
        }

        @Override
        public void recordDrainerTick(DrainerOutcome outcome, Duration took) {
        }

        @Override
        public void recordVerification(String realmName, String clientId,
                                       VerificationInitiator initiator,
                                       VerificationOutcome outcome,
                                       Duration took) {
        }

        @Override
        public void updateOutboxDepthSnapshot(Map<RealmStatus, Long> snapshot) {
        }
    };

    private final MeterRegistry registry;

    /**
     * 缓存的发件箱深度快照，每个 drainer tick 结束时刷新。
     * 仪表从此映射读取；抓取除 {@link ConcurrentHashMap} 查找外无额外开销。
     */
    private volatile Map<RealmStatus, Long> depthSnapshot = Collections.emptyMap();

    /**
     * 跟踪已注册的 {@code (realm, status)} 仪表键，避免重复快照更新时二次注册。
     * Micrometer 的 {@code Gauge#builder} 理论上幂等，但此处防护可减少日志噪音并保持热路径精简。
     */
    private final ConcurrentHashMap<RealmStatus, Boolean> registeredDepthGauges = new ConcurrentHashMap<>();

    /**
     * 最近一次 drainer tick 的 epoch 秒时间戳。由 {@link #recordDrainerTick} 写入；
     * 构造函数绑定的 {@link #METER_DRAINER_TICK_LAST_AT} 仪表读取。
     * 使用 {@code volatile}，因 drainer tick 在调度线程运行而抓取在 HTTP 工作线程。
     */
    private volatile long drainerTickLastAtEpochSeconds = 0L;

    public SsfMetricsBinder() {
        this(Metrics.globalRegistry);
    }

    public SsfMetricsBinder(MeterRegistry registry) {
        this.registry = registry;
        registerDrainerLastTickGauge();
    }

    /**
     * 无标签的单仪表——构造函数中 eagerly 绑定以便抓取立即可读。
     * 供应函数读取 volatile 字段，仪表始终反映最新时间戳。
     */
    private void registerDrainerLastTickGauge() {
        try {
            Gauge.builder(METER_DRAINER_TICK_LAST_AT, this, b -> b.drainerTickLastAtEpochSeconds)
                    .description("Epoch-second of the most recent SSF outbox drainer tick. "
                            + "Operators alert on time() - this > N to detect a stalled drainer. "
                            + "Reports 0 before the first tick.")
                    .baseUnit("seconds")
                    .register(registry);
        } catch (RuntimeException e) {
            // Same swallow pattern as the per-receiver depth gauges:
            // metrics are best-effort, never break drainer behaviour.
            log.warnf(e, "Failed to register %s gauge", METER_DRAINER_TICK_LAST_AT);
        }
    }

    // 仅供 NOOP 使用的私有构造器，跳过注册表 wiring。
    private SsfMetricsBinder(boolean skipRegistry) {
        this.registry = null;
    }

    /**
     * 发件箱深度仪表映射的复合键。
     */
    public record RealmStatus(String realmId, OutboxEntryStatus status) {
    }

    // ---------------------------------------------------------------- 记录

    public void recordEnqueued(String realmId, String clientId, String deliveryMethod, String eventType) {
        counter(METER_EVENTS_ENQUEUED,
                "realm", safe(realmId),
                "client_id", safe(clientId),
                "delivery_method", safe(deliveryMethod),
                "event_type", safe(eventType))
                .increment();
    }

    public void recordSuppressed(String realmId, String clientId, SuppressReason reason) {
        counter(METER_EVENTS_SUPPRESSED,
                "realm", safe(realmId),
                "client_id", safe(clientId),
                "reason", reason.label())
                .increment();
    }

    public void recordPushDelivery(String realmId, String clientId, PushOutcome outcome, Duration took) {
        counter(METER_PUSH_DELIVERY,
                "realm", safe(realmId),
                "client_id", safe(clientId),
                "outcome", outcome.label())
                .increment();
        Timer timer = Timer.builder(METER_PUSH_DELIVERY_DURATION)
                .description("Push delivery duration per outbox row.")
                .tags(Tags.of(
                        Tag.of("realm", safe(realmId)),
                        Tag.of("client_id", safe(clientId)),
                        Tag.of("outcome", outcome.label())))
                .register(registry);
        timer.record(took);
    }

    public void recordPollServed(String realmId, String clientId, long count) {
        if (count <= 0) {
            return;
        }
        counter(METER_POLL_SERVED,
                "realm", safe(realmId),
                "client_id", safe(clientId))
                .increment(count);
    }

    public void recordPollAck(String realmId, String clientId, long count) {
        if (count <= 0) {
            return;
        }
        counter(METER_POLL_ACK,
                "realm", safe(realmId),
                "client_id", safe(clientId))
                .increment(count);
    }

    public void recordPollNack(String realmId, String clientId, long count) {
        if (count <= 0) {
            return;
        }
        counter(METER_POLL_NACK,
                "realm", safe(realmId),
                "client_id", safe(clientId))
                .increment(count);
    }

    public void recordDrainerTick(DrainerOutcome outcome, Duration took) {
        counter(METER_DRAINER_TICK, "outcome", outcome.label()).increment();
        Timer.builder(METER_DRAINER_TICK_DURATION)
                .description("Total SSF outbox drainer tick duration.")
                .register(registry)
                .record(took);
        // 每次 tick（成功或失败）均写入时间戳，使失败但仍 tick 的 drainer 报告新鲜时间；
        // 仅当 drainer 卡住永不返回时仪表才会落后。Time.currentTime()（epoch 秒、墙钟）
        // 为 Keycloak 全站约定，可直接与 Prometheus time() 比较。
        drainerTickLastAtEpochSeconds = Time.currentTime();
    }

    /**
     * 记录一次验证分发。对无耗时测量的结果（ notably {@link VerificationOutcome#RATE_LIMITED}，
     * 在任何 HTTP 推送前即被拒绝）{@code took} 可为 {@code null}。
     */
    public void recordVerification(String realmName,
                                   String clientId,
                                   VerificationInitiator initiator,
                                   VerificationOutcome outcome,
                                   Duration took) {
        counter(METER_VERIFICATION_REQUESTS,
                "realm", safe(realmName),
                "client_id", safe(clientId),
                "initiator", initiator.label(),
                "outcome", outcome.label())
                .increment();
        if (took != null) {
            Timer.builder(METER_VERIFICATION_DURATION)
                    .description("Verification dispatch duration (sync push to receiver).")
                    .tags(Tags.of(
                            Tag.of("realm", safe(realmName)),
                            Tag.of("client_id", safe(clientId)),
                            Tag.of("initiator", initiator.label()),
                            Tag.of("outcome", outcome.label())))
                    .register(registry)
                    .record(took);
        }
    }

    /**
     * 用 drainer tick 产生的快照替换当前发件箱深度快照。
     * 懒注册新出现的 {@code (realm, status)} 仪表；仪表从缓存映射读取，抓取不触库。
     */
    public void updateOutboxDepthSnapshot(Map<RealmStatus, Long> snapshot) {
        Map<RealmStatus, Long> safeSnapshot = snapshot == null ? Collections.emptyMap() : snapshot;
        this.depthSnapshot = safeSnapshot;
        for (RealmStatus key : safeSnapshot.keySet()) {
            ensureDepthGauge(key);
        }
    }

    private void ensureDepthGauge(RealmStatus key) {
        if (registeredDepthGauges.putIfAbsent(key, Boolean.TRUE) != null) {
            return;
        }
        try {
            Gauge.builder(METER_OUTBOX_DEPTH, depthSnapshot, m -> {
                        Long v = m.get(key);
                        return v == null ? 0.0 : v.doubleValue();
                    })
                    .description("Outbox row count, snapshot from last drainer tick.")
                    .baseUnit(BaseUnits.ROWS)
                    .tags(Tags.of(
                            Tag.of("realm", safe(key.realmId())),
                            Tag.of("status", key.status().name())))
                    .register(registry);
        } catch (RuntimeException e) {
            // Never let a meter registration failure propagate back
            // into the drainer — the drainer's job is draining, not
            // metrics hygiene.
            log.debugf(e, "Failed to register SSF outbox depth gauge for %s", key);
            registeredDepthGauges.remove(key);
        }
    }

    // ------------------------------------------------------------ 内部

    private Counter counter(String name, String... tagPairs) {
        return Counter.builder(name)
                .tags(tagPairs)
                .register(registry);
    }

    /**
     * Prometheus 标签值必须为字符串；启动竞态中的 null 客户端/realm
     * 变为字面量 {@code "unknown"}，避免计量器静默丢弃增量。
     */
    private static String safe(String value) {
        return value == null || value.isEmpty() ? "unknown" : value;
    }
}
