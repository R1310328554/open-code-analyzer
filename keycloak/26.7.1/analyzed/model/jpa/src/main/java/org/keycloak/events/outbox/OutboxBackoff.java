/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.events.outbox;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 计算投递失败后的指数退避下次重试时间，并判定行是否应转入 {@code DEAD_LETTER}。
 * <p>
 * 退避曲线与死信阈值由 {@link OutboxConfig#backoff()} 按 kind 配置，
 * SSF push、webhook 等消费者可选用不同语义。
 * </p>
 * <p>
 * 每次计算混入 ±25% 均匀抖动，避免同一时刻大量入队的行在同一毫秒唤醒，
 * 在集群部署中分散重试负载。
 * </p>
 */
public class OutboxBackoff {

    /** 默认最大重试次数。 */
    public static final int DEFAULT_MAX_ATTEMPTS = 8;

    /**
     * 默认 HTTP push 退避曲线——适用于 HTTP POST 类接收方；
     * 其他语义（如内部队列写入）应提供自定义曲线。
     */
    public static final List<Duration> DEFAULT_HTTP_PUSH_CURVE = List.of(
            Duration.ofSeconds(1),
            Duration.ofSeconds(5),
            Duration.ofSeconds(30),
            Duration.ofMinutes(2),
            Duration.ofMinutes(10),
            Duration.ofHours(1),
            Duration.ofHours(6),
            Duration.ofHours(24)
    );

    protected final int maxAttempts;
    protected final List<Duration> curve;

    public OutboxBackoff() {
        this(DEFAULT_MAX_ATTEMPTS, DEFAULT_HTTP_PUSH_CURVE);
    }

    public OutboxBackoff(int maxAttempts, List<Duration> curve) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1, got " + maxAttempts);
        }
        Objects.requireNonNull(curve, "curve");
        if (curve.isEmpty()) {
            throw new IllegalArgumentException("curve must not be empty");
        }
        this.maxAttempts = maxAttempts;
        this.curve = curve;
    }

    /**
     * 重试预算是否已耗尽，应转入 {@code DEAD_LETTER} 而非再调度。
     *
     * @param attempts 计入当前失败后 {@code attempts} 的值。
     */
    public boolean isExhausted(int attempts) {
        return attempts >= maxAttempts;
    }

    /**
     * 失败后 {@code attempts} 已递增，计算 {@code next_attempt_at}。
     * 仅在 {@link #isExhausted(int)} 为 false 时调用。
     */
    public Instant computeNextAttemptAt(Instant now, int attempts) {
        Duration base = baseDelayFor(attempts);
        long baseMillis = base.toMillis();
        long jitterRangeMillis = Math.max(1, baseMillis / 4);
        long jitterMillis = ThreadLocalRandom.current()
                .nextLong(-jitterRangeMillis, jitterRangeMillis + 1);
        long delayMillis = Math.max(0, baseMillis + jitterMillis);
        return now.plusMillis(delayMillis);
    }

    /**
     * 曲线前 {@link #maxAttempts} 项之和——行在自然重试路径下于 PENDING 状态
     * 可能停留的最长累计时间。运维设置 {@code pendingMaxAge} 兜底时应高于此值。
     */
    public Duration getMaxNaturalRetryDuration() {
        Duration total = Duration.ZERO;
        int n = Math.min(maxAttempts, curve.size());
        for (int i = 0; i < n; i++) {
            total = total.plus(curve.get(i));
        }
        return total;
    }

    protected Duration baseDelayFor(int attempts) {
        // attempts 在首次失败后从 1 起计。
        int idx = Math.min(Math.max(attempts, 1), curve.size()) - 1;
        return curve.get(idx);
    }

    public List<Duration> getCurve() {
        return curve;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}
