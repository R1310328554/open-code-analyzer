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
 * 基于 Cron 表达式的调度任务参数。
 * <p>
 * 使用 {@link org.redisson.executor.CronExpression} 计算下次触发时间。
 *
 * @author Nikita Koksharov
 *
 */
public class ScheduledCronExpressionParameters extends ScheduledParameters {

    /** Cron 表达式字符串（Quartz 语法）。 */
    private String cronExpression;
    /** 解析 Cron 时使用的时区 ID。 */
    private String timezone; 
    /** 目标执行器实例 ID。 */
    private String executorId;

    /** 无参构造。 */
    public ScheduledCronExpressionParameters() {
    }

    /** @param requestId 任务请求 ID */
    public ScheduledCronExpressionParameters(String requestId) {
        super(requestId);
    }

    /** 返回 Cron 表达式。 */
    public String getCronExpression() {
        return cronExpression;
    }
    /** 设置 Cron 表达式。 */
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
    /** 返回时区 ID。 */
    public String getTimezone() {
        return timezone;
    }
    /** 设置 Cron 解析时区。 */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
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
