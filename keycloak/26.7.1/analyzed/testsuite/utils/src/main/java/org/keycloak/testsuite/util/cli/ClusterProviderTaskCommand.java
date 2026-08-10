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

package org.keycloak.testsuite.util.cli;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.keycloak.cluster.ClusterProvider;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.models.KeycloakSession;

/**
 * 集群提供者任务测试命令，验证 {@link ClusterProvider#executeIfNotExecutedAsync}
 * 在指定超时内仅执行一次的异步任务语义。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClusterProviderTaskCommand extends AbstractCommand {

    /** 用于在后台等待集群任务完成的线程池。 */
    private static final ExecutorService executors = Executors.newCachedThreadPool();

    /**
     * 提交集群异步任务并在独立线程中等待其完成。
     * 参数依次为：任务名、任务超时（秒）、任务内睡眠时长（秒）。
     */
    @Override
    protected void doRunCommand(KeycloakSession session) {
        String taskName = getArg(0);
        int taskTimeout = getIntArg(1);
        int sleepTime = getIntArg(2);

        // 获取集群提供者并提交去重异步任务
        ClusterProvider cluster = session.getProvider(ClusterProvider.class);
        Future future = cluster.executeIfNotExecutedAsync(taskName, taskTimeout, () -> {
            log.infof("Started sleeping for " + sleepTime + " seconds");
            Thread.sleep(sleepTime * 1000);
            log.infof("Stopped sleeping");
            return null;
        });

        log.info("I've retrieved future successfully");

        // 在独立线程中阻塞等待 Future 完成
        executors.execute(() -> {
            try {
                future.get();
                log.info("Successfully finished future!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /** 将等待时间写入配置映射（辅助方法，当前命令未调用）。 */
    private void updateConfig(MultivaluedHashMap<String, String> cfg, int waitTime) {
        cfg.putSingle("wait-time", String.valueOf(waitTime));
    }


    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "clusterProviderTask";
    }

    /** {@inheritDoc} */
    @Override
    public String printUsage() {
        return super.printUsage() + " <task-name> <task-wait-time-in-seconds> <sleep-time-in-seconds>";
    }
}
