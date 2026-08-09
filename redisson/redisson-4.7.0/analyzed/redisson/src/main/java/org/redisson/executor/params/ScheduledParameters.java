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
 * 一次性或带开始时间的调度任务参数基类。
 * <p>
 * 在 {@link TaskParameters} 基础上增加 {@code startTime}（毫秒时间戳）。
 *
 * @author Nikita Koksharov
 *
 */
public class ScheduledParameters extends TaskParameters {

    /** 计划开始执行的 Unix 毫秒时间戳。 */
    private long startTime;

    /** 无参构造。 */
    public ScheduledParameters() {
    }

    /** @param requestId 任务请求 ID */
    public ScheduledParameters(String requestId) {
        super(requestId);
    }

    /** 完整构造，包含序列化载荷与开始时间。 */
    public ScheduledParameters(String id, String className, byte[] classBody, byte[] lambdaBody, byte[] state, long startTime) {
        super(id, className, classBody, lambdaBody, state);
        this.startTime = startTime;
    }

    /** 返回计划开始时间戳。 */
    public long getStartTime() {
        return startTime;
    }

    /** 设置计划开始时间戳。 */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
}
