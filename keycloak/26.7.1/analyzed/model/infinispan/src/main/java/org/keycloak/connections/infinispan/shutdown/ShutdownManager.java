/*
 * Copyright 2026 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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

package org.keycloak.connections.infinispan.shutdown;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.keycloak.common.util.Time;

import org.jboss.logging.Logger;

/**
 * 管理有序注册的 {@link ShutdownListener} 列表，在服务器关闭时按序通知各监听器。
 * <p>
 * 监听器按注册顺序调用。每个监听器可阻塞调用线程以延迟关闭，直到其条件满足
 * （例如等待缓存拓扑稳定）。所有监听器共享同一截止时间，因此无论注册多少监听器，
 * 总关闭时长都有上界。建议各监听器严格遵守该截止时间。
 * <p>
 * 若 {@link #onShutdownStarted(Instant)} 在 {@link #onShutdown()} 之前被调用
 * （例如 Quarkus 关闭延迟事件），则截止时间锚定到较早的时间戳；否则以
 * {@link #onShutdown()} 调用时的当前时间为准。
 * <p>
 * 本类线程安全：可在运行时并发添加或移除监听器。
 */
public class ShutdownManager {

    private static final Logger logger = Logger.getLogger(ShutdownManager.class);

    /** 按注册顺序保存的关闭监听器列表（写时复制，支持并发注册）。 */
    private final List<ShutdownListener> listeners = new CopyOnWriteArrayList<>();
    /** 关闭延迟与关闭超时之和，构成全局最大等待毫秒数。 */
    private final long maxShutdownTimeout;
    /** 关闭序列实际开始时刻（可能早于 {@link #onShutdown()} 调用）。 */
    private volatile Instant shutdownStartTime;

    /**
     * @param shutdownDelay   关闭延迟（毫秒），计入总超时预算
     * @param shutdownTimeout 关闭超时（毫秒），计入总超时预算
     */
    public ShutdownManager(long shutdownDelay, long shutdownTimeout) {
        this.maxShutdownTimeout = shutdownDelay + shutdownTimeout;
    }

    /** 注册一个关闭监听器，按调用顺序在关闭时依次执行。 */
    public void addListener(ShutdownListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    /** 移除先前注册的关闭监听器。 */
    public void removeListener(ShutdownListener listener) {
        listeners.remove(Objects.requireNonNull(listener));
    }

    /** 触发关闭流程：计算共享截止时间并依次通知所有监听器。 */
    public void onShutdown() {
        var instant = Objects.requireNonNullElse(shutdownStartTime, Instant.ofEpochMilli(Time.currentTimeMillis()));
        var deadline = Date.from(instant.plus(maxShutdownTimeout, ChronoUnit.MILLIS));
        for (var listener : listeners) {
            try {
                listener.onShutdown(instant, deadline);
            } catch (Exception e) {
                logger.warnf(e, "Shutdown listener %s failed", listener);
            }
        }
    }

    /**
     * 记录关闭序列开始的时刻（例如 Quarkus 发起关闭延迟时）。
     *
     * @param shutdownStartTime 关闭发起时刻
     */
    public void onShutdownStarted(Instant shutdownStartTime) {
        this.shutdownStartTime = shutdownStartTime;
    }
}
