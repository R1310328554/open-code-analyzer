/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.client;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于连接或 PING 失败持续时间检测 Redis 节点是否故障。
 * <p>
 * 自首次连接/PING 失败起，若持续超过 {@code checkInterval} 则判定节点不可用。
 *
 * @author Nikita Koksharov
 *
 */
public class FailedConnectionDetector implements FailedNodeDetector {

    /** 连续失败判定阈值（毫秒），默认 180000。 */
    private long checkInterval;

    /** 首次连接或 PING 失败的时间戳，0 表示当前无失败。 */
    private final AtomicLong firstFailTime = new AtomicLong(0);

    /** 使用默认 180 秒检测窗口。 */
    public FailedConnectionDetector() {
        this(180000);
    }

    public FailedConnectionDetector(long checkInterval) {
        if (checkInterval == 0) {
            throw new IllegalArgumentException("checkInterval value");
        }

        this.checkInterval = checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        if (checkInterval == 0) {
            throw new IllegalArgumentException("checkInterval value");
        }

        this.checkInterval = checkInterval;
    }

    @Override
    public void onConnectFailed() {
    }

    /** 记录首次连接失败时间（若尚未记录）。 */
    @Override
    public void onConnectFailed(Throwable cause) {
        firstFailTime.compareAndSet(0, System.currentTimeMillis());
    }

    /** 连接成功后清除失败计时。 */
    @Override
    public void onConnectSuccessful() {
        firstFailTime.set(0);
    }

    @Override
    public void onPingSuccessful() {
        firstFailTime.set(0);
    }

    @Override
    public void onCommandSuccessful() {
    }

    @Override
    public void onPingFailed() {
    }

    @Override
    public void onPingFailed(Throwable cause) {
        firstFailTime.compareAndSet(0, System.currentTimeMillis());
    }

    @Override
    public void onCommandFailed(Throwable cause) {
    }

    /** 判断自首次失败起是否已超过检测窗口。 */
    @Override
    public boolean isNodeFailed() {
        if (firstFailTime.get() != 0 && checkInterval > 0) {
            return System.currentTimeMillis() - firstFailTime.get() > checkInterval;
        }

        return false;
    }

    @Override
    public FailedNodeDetector copy() {
        return new FailedConnectionDetector(checkInterval);
    }

}
