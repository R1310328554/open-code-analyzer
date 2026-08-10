/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.workflow;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.keycloak.Config.Scope;
import org.keycloak.common.Profile;
import org.keycloak.common.util.DurationConverter;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.EnvironmentDependentProviderFactory;
import org.keycloak.provider.ProviderEvent;
import org.keycloak.services.scheduled.ClusterAwareScheduledTaskRunner;
import org.keycloak.timer.TimerProvider;

import org.jboss.logging.Logger;

/**
 * 工作流事件监听器工厂，ID 为 {@code workflow-event-listener}。
 * <p>创建全局 {@link WorkflowEventListener}，注册 {@link ProviderEvent} 回调，并按配置间隔调度 {@link WorkflowRunnerScheduledTask} 集群感知定时任务。</p>
 */
public class WorkflowsEventListenerFactory implements EventListenerProviderFactory, EnvironmentDependentProviderFactory {

    private static final Logger logger = Logger.getLogger(WorkflowsEventListenerFactory.class);

    /** 事件监听器工厂标识 {@code workflow-event-listener}。 */
    public static final String ID = "workflow-event-listener";
    private static final long DEFAULT_STEP_RUNNER_TASK_INTERVAL = Duration.ofHours(12).toMillis();
    private long stepRunnerTaskInterval;
    private LocalTime stepRunnerTaskStartTime;

    /** 创建绑定当前会话的 {@link WorkflowEventListener}。 */
    @Override
    public EventListenerProvider create(KeycloakSession session) {
        return new WorkflowEventListener(session);
    }

    /** @return 全局监听器，跨 Realm 共享实例 */
    @Override
    public boolean isGlobal() {
        return true;
    }

    /** 从配置读取步骤运行器间隔 {@code stepRunnerTaskInterval} 与起始时间 {@code stepRunnerTaskStartTime}。 */
    @Override
    public void init(Scope config) {
        String taskIntervalStr = config.get("stepRunnerTaskInterval");
        this.stepRunnerTaskInterval = taskIntervalStr == null ? DEFAULT_STEP_RUNNER_TASK_INTERVAL : DurationConverter.parseDuration(taskIntervalStr).toMillis();

        String startTimeStr = config.get("stepRunnerTaskStartTime");
        if (startTimeStr != null) {
            try {
                this.stepRunnerTaskStartTime = LocalTime.parse(startTimeStr);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid stepRunnerTaskStartTime value '" + startTimeStr
                        + "'. Expected format: HH:mm (e.g., 02:00, 14:30)", e);
            }
        }
    }

    /** 注册 Provider 事件回调并调度 {@link WorkflowRunnerScheduledTask}。 */
    @Override
    public void postInit(KeycloakSessionFactory factory) {
        factory.register(event -> {
            KeycloakSession session = event.getKeycloakSession();

            if (session != null) {
                onEvent(event, session);
            }
        });
        scheduleStepRunnerTask(factory);
    }

    /** 将 {@link ProviderEvent} 转发至工作流事件监听器。 */
    private void onEvent(ProviderEvent event, KeycloakSession session) {
        WorkflowEventListener provider = (WorkflowEventListener) session.getProvider(EventListenerProvider.class, getId());
        provider.onEvent(event);
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return ID;
    }

    /** @return 是否启用 {@link Profile.Feature#WORKFLOWS} 功能 */
    @Override
    public boolean isSupported(Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.WORKFLOWS);
    }

    /** 通过 {@link ClusterAwareScheduledTaskRunner} 注册集群感知定时任务。 */
    private void scheduleStepRunnerTask(KeycloakSessionFactory factory) {
        long initialDelay = computeInitialDelay();

        try (KeycloakSession session = factory.create()) {
            TimerProvider timer = session.getProvider(TimerProvider.class);
            ClusterAwareScheduledTaskRunner runner = new ClusterAwareScheduledTaskRunner(factory,
                    new WorkflowRunnerScheduledTask(factory), stepRunnerTaskInterval);
            timer.schedule(runner, initialDelay, stepRunnerTaskInterval);
        }

        ZonedDateTime nextExecution = ZonedDateTime.now().plus(Duration.ofMillis(initialDelay));
        logger.infof("Workflow runner task scheduled: next execution at %s, then every %s",
                nextExecution.toLocalTime().withNano(0),
                Duration.ofMillis(stepRunnerTaskInterval));
    }

    /**
     * 计算首次定时任务执行前的初始延迟。
     * <p>
     * 若配置了起始时间，则以其为锚点对齐执行网格（例如 18:00 起始、2 小时间隔 → 00:00、02:00…）。初始延迟使首次执行落在当前时间之后的下一网格点。
     * <p>
     * 未配置起始时间时，初始延迟等于执行间隔（默认行为）。
     */
    long computeInitialDelay() {
        if (stepRunnerTaskStartTime == null) {
            return stepRunnerTaskInterval;
        }
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime anchor = now.toLocalDate().atTime(stepRunnerTaskStartTime).atZone(now.getZone());
        long millisPastLastGridPoint = Math.floorMod(Duration.between(anchor, now).toMillis(), stepRunnerTaskInterval);
        return stepRunnerTaskInterval - millisPastLastGridPoint;
    }
}
