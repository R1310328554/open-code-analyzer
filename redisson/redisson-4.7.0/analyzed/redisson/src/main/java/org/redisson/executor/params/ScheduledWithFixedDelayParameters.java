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
package org.redisson.executor.params;

/**
 * 固定延迟（fixed-delay）周期调度任务参数。
 * <p>
 * 每次执行完成后等待 {@code delay} 毫秒再调度下一轮。
 *
 * @author Nikita Koksharov
 *
 */
public class ScheduledWithFixedDelayParameters extends ScheduledParameters {

    /** 两次执行之间的固定延迟（毫秒）。 */
    private long delay; 
    /** 目标执行器实例 ID。 */
    private String executorId;

    /** 无参构造。 */
    public ScheduledWithFixedDelayParameters() {
    }

    /** @param requestId 固定任务请求 ID */
    public ScheduledWithFixedDelayParameters(String requestId) {
        super(requestId);
    }

    /** 返回 fixed-delay 间隔。 */
    public long getDelay() {
        return delay;
    }
    /** 设置执行间隔延迟（毫秒）。 */
    public void setDelay(long delay) {
        this.delay = delay;
    }
    
    /** 返回目标执行器 ID。 */
    public String getExecutorId() {
        return executorId;
    }
    /** 设置目标执行器 ID。 */
    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }
    
}
