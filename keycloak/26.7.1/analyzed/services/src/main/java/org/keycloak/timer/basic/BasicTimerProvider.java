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

package org.keycloak.timer.basic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.keycloak.common.util.Time;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.scheduled.ScheduledTaskRunner;
import org.keycloak.timer.ScheduledTask;
import org.keycloak.timer.TimerProvider;

import org.jboss.logging.Logger;

/**
 * 基于 {@link java.util.Timer} 的基础定时任务提供者。
 * <p>在 Keycloak 会话范围内调度 Runnable 或 {@link org.keycloak.timer.ScheduledTask}，支持按任务名取消与替换已有任务。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class BasicTimerProvider implements TimerProvider {

    private static final Logger logger = Logger.getLogger(BasicTimerProvider.class);

    private final KeycloakSession session;
    private final Timer timer;
    private final int transactionTimeout;
    private final BasicTimerProviderFactory factory;

    /** 绑定会话、共享 Timer 实例与工厂（用于任务注册表）。 */
    public BasicTimerProvider(KeycloakSession session, Timer timer, int transactionTimeout, BasicTimerProviderFactory factory) {
        this.session = session;
        this.timer = timer;
        this.transactionTimeout = transactionTimeout;
        this.factory = factory;
    }

    /** 以相同初始延迟与间隔调度 Runnable。 */
    @Override
    public void schedule(final Runnable runnable, final long intervalMillis, String taskName) {
        schedule(runnable, intervalMillis, intervalMillis, taskName);
    }

    /** 调度 Runnable；同名任务会先取消再注册。 */
    @Override
    public void schedule(final Runnable runnable, final long initialDelayMillis, final long intervalMillis, String taskName) {
        TimerTask task = new BasicTimerTask(runnable);

        TimerTaskContextImpl taskContext = new TimerTaskContextImpl(runnable, task, Time.currentTimeMillis(), intervalMillis);
        TimerTaskContextImpl existingTask = factory.putTask(taskName, taskContext);
        if (existingTask != null) {
            logger.debugf("Existing timer task '%s' found. Cancelling it", taskName);
            existingTask.timerTask.cancel();
        }

        logger.debugf("Starting task '%s' with initial delay '%d' and interval '%d'", taskName, initialDelayMillis, intervalMillis);
        timer.schedule(task, initialDelayMillis, intervalMillis);
    }

    /** 包装 ScheduledTask 为 ScheduledTaskRunner 并调度。 */
    @Override
    public void scheduleTask(ScheduledTask scheduledTask, long intervalMillis, String taskName) {
        ScheduledTaskRunner scheduledTaskRunner = new ScheduledTaskRunner(session.getKeycloakSessionFactory(), scheduledTask, transactionTimeout);
        this.schedule(scheduledTaskRunner, intervalMillis, taskName);
    }

    /** 包装 ScheduledTask 并按指定延迟与间隔调度。 */
    @Override
    public void scheduleTask(ScheduledTask scheduledTask, long initialDelayMillis, long intervalMillis, String taskName) {
        ScheduledTaskRunner scheduledTaskRunner = new ScheduledTaskRunner(session.getKeycloakSessionFactory(), scheduledTask, transactionTimeout);
        this.schedule(scheduledTaskRunner, initialDelayMillis, intervalMillis, taskName);
    }

    /** 取消并移除指定名称的任务，返回其上下文（若存在）。 */
    @Override
    public TimerTaskContext cancelTask(String taskName) {
        TimerTaskContextImpl existingTask = factory.removeTask(taskName);
        if (existingTask != null) {
            logger.debugf("Cancelling task '%s'", taskName);
            existingTask.timerTask.cancel();
        }

        return existingTask;
    }

    /** 关闭提供者（当前无额外清理）。 */
    @Override
    public void close() {
        // 无需释放资源
    }

    /** 返回当前已注册任务的不可变快照。 */
    @Override
    public Map<String, TimerTaskContext> getTasks() {
        return Collections.unmodifiableMap(new HashMap<>(factory.getTasks()));
    }

    /**
     * 私有静态内部类避免 TimerTask 持有 {@link BasicTimerProvider} 引用，防止 {@link KeycloakSession} 无法被 GC 回收。
     */
    private static class BasicTimerTask extends TimerTask {
        private final Runnable runnable;

        public BasicTimerTask(Runnable runnable) {
            this.runnable = runnable;
        }

        /** 执行包装的 Runnable。 */
        @Override
        public void run() {
            runnable.run();
        }
    }
}
