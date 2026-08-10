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

package org.keycloak.services.scheduled;

import org.keycloak.logging.MappedDiagnosticContextUtil;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.timer.ScheduledTask;
import org.keycloak.timer.TaskRunner;
import org.keycloak.tracing.TracingProvider;

import org.jboss.logging.Logger;

/**
 * 定时任务运行器：在 Keycloak 事务上下文中执行 {@link ScheduledTask}。
 * <p>由 {@link TaskRunner} 调度，支持可选的事务元素上限与链路追踪。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ScheduledTaskRunner implements TaskRunner {

    private static final Logger logger = Logger.getLogger(ScheduledTaskRunner.class);

    /** Keycloak 会话工厂，用于创建事务性会话 */
    protected final KeycloakSessionFactory sessionFactory;

    /** 待执行的定时任务 */
    protected final ScheduledTask task;

    /** 单次运行允许的最大事务元素数，{@code 0} 表示不限制 */
    protected final int transactionLimit;

    /** 构造运行器，不限制事务元素数量。 */
    public ScheduledTaskRunner(KeycloakSessionFactory sessionFactory, ScheduledTask task) {
        this(sessionFactory, task, 0);
    }

    /**
     * 构造运行器。
     * @param sessionFactory 会话工厂
     * @param task 定时任务
     * @param transactionLimit 事务元素上限，{@code 0} 表示不限制
     */
    public ScheduledTaskRunner(KeycloakSessionFactory sessionFactory, ScheduledTask task, int transactionLimit) {
        this.sessionFactory = sessionFactory;
        this.task = task;
        this.transactionLimit = transactionLimit;
    }

    /** 在追踪与事务上下文中执行定时任务，异常时记录错误日志。 */
    @Override
    public void run() {
        // 直接追踪以避免创建不必要的事务及多余的 JTA 事务元素
        TracingProvider tracing = sessionFactory.getProviderFactory(TracingProvider.class).create(null);
        try {
            tracing.trace("ScheduledTaskRunner", task.getTaskName() + ".run", span -> {
                KeycloakModelUtils.runJobInTransaction(sessionFactory, new NamedSessionTask("Scheduled task: " + task.getTaskName()) {

                    @Override
                    public void run(KeycloakSession session) {
                        try {
                            if (transactionLimit != 0) {
                                KeycloakModelUtils.setTransactionLimit(sessionFactory, transactionLimit);
                            }

                            runTask(session);
                        } finally {
                            if (transactionLimit != 0) {
                                KeycloakModelUtils.setTransactionLimit(sessionFactory, 0);
                            }
                        }
                    }
                });
            });
        } catch (Throwable t) {
            logger.errorf(t, "Failed to run scheduled task %s", task.getTaskName());
        } finally {
            tracing.close();
            MappedDiagnosticContextUtil.clearMdc();
        }
    }

    /** 执行具体任务逻辑并输出调试日志。 */
    protected void runTask(KeycloakSession session) {
        task.run(session);

        logger.debugf("Executed scheduled task %s", task.getTaskName());
    }

    /** @return 被包装的 {@link ScheduledTask} */
    @Override
    public ScheduledTask getTask() {
        return task;
    }
}
