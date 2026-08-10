/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.timer;

import java.util.Map;

import org.keycloak.provider.Provider;

/**
 * 定时器提供者：调度周期性任务与 {@link ScheduledTask}。
 * <p>支持按名称取消任务并查询运行中的任务上下文。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface TimerProvider extends Provider {

    /**
     * 按固定间隔调度 {@link Runnable}。
     * @param runnable 待执行任务
     * @param intervalMillis 执行间隔（毫秒）
     * @param taskName 任务唯一名称
     */
    public void schedule(Runnable runnable, long intervalMillis, String taskName);

    /**
     * 调度任务，首次执行延迟可与后续间隔不同。
     *
     * @param runnable the task to run
     * @param initialDelayMillis delay before the first execution
     * @param intervalMillis interval between subsequent executions
     * @param taskName unique name for the task
     */
    default void schedule(Runnable runnable, long initialDelayMillis, long intervalMillis, String taskName) {
        schedule(runnable, intervalMillis, taskName);
    }

    /** 使用 {@link TaskRunner#getTaskName()} 作为名称调度任务。 */
    default void schedule(TaskRunner runner, long intervalMillis) {
        schedule(runner, intervalMillis, runner.getTaskName());
    }

    default void schedule(TaskRunner runner, long initialDelayMillis, long intervalMillis) {
        schedule(runner, initialDelayMillis, intervalMillis, runner.getTaskName());
    }

    /**
     * 调度 {@link ScheduledTask}。
     * @param scheduledTask 定时任务
     * @param intervalMillis 执行间隔（毫秒）
     * @param taskName 任务唯一名称
     */
    public void scheduleTask(ScheduledTask scheduledTask, long intervalMillis, String taskName);

    public default void scheduleTask(ScheduledTask scheduledTask, long intervalMillis) {
        scheduleTask(scheduledTask, intervalMillis, scheduledTask.getTaskName());
    }

    public default void scheduleTask(ScheduledTask scheduledTask, long initialDelayMillis, long intervalMillis) {
        scheduleTask(scheduledTask, initialDelayMillis, intervalMillis, scheduledTask.getTaskName());
    }

    public default void scheduleTask(ScheduledTask scheduledTask, long initialDelayMillis, long intervalMillis, String taskName) {
        scheduleTask(scheduledTask, intervalMillis, taskName);
    }

    /**
     * 取消任务并返回其上下文，以便后续恢复。
     *
     * @param taskName
     * @return existing task or null if task under this name doesn't exist
     */
    public TimerTaskContext cancelTask(String taskName);

    /** @return 当前已调度任务的名称到上下文映射 */
    public Map<String, TimerTaskContext> getTasks();

    /** 已调度任务的运行上下文。 */
    interface TimerTaskContext {

        /** @return 任务 Runnable */
        Runnable getRunnable();

        /** @return 任务启动时间（毫秒） */
        long getStartTimeMillis();

        /** @return 任务执行间隔（毫秒） */
        long getIntervalMillis();
    }

}
