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

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 基于命令执行失败次数检测 Redis 节点是否故障。
 * <p>
 * 在 {@code checkInterval} 时间窗口内，若失败次数达到 {@code failedCommandsLimit} 则判定节点不可用。
 *
 * @author Nikita Koksharov
 *
 */
public class FailedCommandsDetector implements FailedNodeDetector {

    /** 统计失败次数的时间窗口（毫秒）。 */
    protected long checkInterval;

    /** 窗口内触发故障判定的失败次数阈值。 */
    protected long failedCommandsLimit;

    /** 记录各次命令失败的时间戳，用于滑动窗口统计。 */
    private final NavigableSet<Long> failedCommands = new ConcurrentSkipListSet<>();

    public FailedCommandsDetector() {
    }

    /** 指定检测窗口与失败次数阈值。 */
    public FailedCommandsDetector(long checkInterval, int failedCommandsLimit) {
        if (checkInterval == 0) {
            throw new IllegalArgumentException("checkInterval value");
        }
        if (failedCommandsLimit == 0) {
            throw new IllegalArgumentException("failedCommandsLimit value");
        }
        this.checkInterval = checkInterval;
        this.failedCommandsLimit = failedCommandsLimit;
    }

    public void setCheckInterval(long checkInterval) {
        if (checkInterval == 0) {
            throw new IllegalArgumentException("checkInterval value");
        }
        this.checkInterval = checkInterval;
    }

    public void setFailedCommandsLimit(long failedCommandsLimit) {
        if (failedCommandsLimit == 0) {
            throw new IllegalArgumentException("failedCommandsLimit value");
        }
        this.failedCommandsLimit = failedCommandsLimit;
    }

    @Override
    public void onConnectFailed() {
    }

    @Override
    public void onConnectFailed(Throwable cause) {
    }

    @Override
    public void onConnectSuccessful() {
    }

    @Override
    public void onPingSuccessful() {
    }

    @Override
    public void onCommandSuccessful() {
    }

    @Override
    public void onPingFailed() {
    }

    @Override
    public void onPingFailed(Throwable cause) {
    }

    /** 记录一次命令失败的时间戳。 */
    @Override
    public void onCommandFailed(Throwable cause) {
        failedCommands.add(System.currentTimeMillis());
    }

    /** 清理过期失败记录，判断窗口内失败次数是否达到阈值。 */
    @Override
    public boolean isNodeFailed() {
        if (failedCommandsLimit == 0) {
            throw new IllegalArgumentException("failedCommandsLimit isn't set");
        }

        long start = System.currentTimeMillis() - checkInterval;
        failedCommands.headSet(start).clear();

        if (failedCommands.tailSet(start).size() >= failedCommandsLimit) {
            failedCommands.clear();
            return true;
        }
        return false;
    }

    /** 返回相同配置但独立运行时状态的新检测器实例。 */
    @Override
    public FailedNodeDetector copy() {
        return new FailedCommandsDetector(checkInterval, (int) failedCommandsLimit);
    }

}
