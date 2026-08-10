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

import java.util.concurrent.Callable;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.cluster.ExecutionResult;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.timer.ScheduledTask;

import org.jboss.logging.Logger;

/**
 * 集群感知的定时任务执行器：确保同一任务在本机或集群其他节点上不会并发执行。
 * <p>
 * 通过 {@link ClusterProvider#executeIfNotExecuted} 在集群范围内以任务类名为键进行互斥调度。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClusterAwareScheduledTaskRunner extends ScheduledTaskRunner {

    private static final Logger logger = Logger.getLogger(ClusterAwareScheduledTaskRunner.class);

    /** 任务调度间隔（秒），用于集群互斥窗口。 */
    private final int intervalSecs;

    public ClusterAwareScheduledTaskRunner(KeycloakSessionFactory sessionFactory, ScheduledTask task, long intervalMillis) {
        super(sessionFactory, task);
        this.intervalSecs = (int) (intervalMillis / 1000);
    }

    @Override
    protected void runTask(final KeycloakSession session) {
        ClusterProvider clusterProvider = session.getProvider(ClusterProvider.class);
        String taskKey = task.getClass().getSimpleName();

        // 复制 task 引用：父类位于另一模块，WildFly 环境下 lambda 无法直接访问
        ScheduledTask localTask = this.task;
        ExecutionResult<Void> result = clusterProvider.executeIfNotExecuted(taskKey, intervalSecs, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                localTask.run(session);
                return null;
            }

        });

        if (result.isExecuted()) {
            logger.debugf("Executed scheduled task %s", taskKey);
        } else {
            logger.debugf("Skipped execution of task %s as other cluster node is executing it", taskKey);
        }
    }


}
