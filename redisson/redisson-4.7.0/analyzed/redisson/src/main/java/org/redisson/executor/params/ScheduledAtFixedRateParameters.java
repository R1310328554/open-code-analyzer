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
 * 固定频率（fixed-rate）周期调度任务参数。
 * <p>
 * 继承 {@link ScheduledParameters}，附加周期 {@code period} 与已耗时长 {@code spentTime}，
 * 用于补偿单次执行耗时对下次触发时间的影响。
 *
 * @author Nikita Koksharov
 *
 */
public class ScheduledAtFixedRateParameters extends ScheduledParameters {

    /** 周期间隔（毫秒）。 */
    private long period;
    /** 目标执行器实例 ID。 */
    private String executorId;
    /** 累计已消耗执行时间（毫秒），用于 fixed-rate 补偿。 */
    private long spentTime;

    /** 无参构造，供序列化框架使用。 */
    public ScheduledAtFixedRateParameters() {
    }

    /** @param requestId 固定任务请求 ID */
    public ScheduledAtFixedRateParameters(String requestId) {
        super(requestId);
    }

    /** 返回累计执行耗时（毫秒）。 */
    public long getSpentTime() {
        return spentTime;
    }
    /** 设置累计执行耗时。 */
    public void setSpentTime(long spentTime) {
        this.spentTime = spentTime;
    }

    /** 返回 fixed-rate 周期间隔。 */
    public long getPeriod() {
        return period;
    }
    /** 设置周期间隔（毫秒）。 */
    public void setPeriod(long period) {
        this.period = period;
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
