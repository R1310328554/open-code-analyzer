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

/**
 * 仅统计命令超时（{@link RedisTimeoutException}）的节点故障检测器。
 * <p>
 * 在 {@code checkInterval} 窗口内超时次数达到 {@code failedCommandsLimit} 时判定节点故障。
 *
 * @author Nikita Koksharov
 *
 */
public class FailedCommandsTimeoutDetector extends FailedCommandsDetector {

    public FailedCommandsTimeoutDetector() {
    }

    public FailedCommandsTimeoutDetector(long checkInterval, int failedCommandsLimit) {
        super(checkInterval, failedCommandsLimit);
    }

    /** 仅当失败原因为超时时才计入失败次数。 */
    @Override
    public void onCommandFailed(Throwable cause) {
        if (cause instanceof RedisTimeoutException) {
            super.onCommandFailed(cause);
        }
    }

    /** 返回相同配置但独立运行时状态的新超时检测器实例。 */
    @Override
    public FailedNodeDetector copy() {
        return new FailedCommandsTimeoutDetector(checkInterval, (int) failedCommandsLimit);
    }

}
