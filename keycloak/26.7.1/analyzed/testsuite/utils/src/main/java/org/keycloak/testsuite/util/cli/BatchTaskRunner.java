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

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.KeycloakSessionTask;
import org.keycloak.models.utils.KeycloakModelUtils;

/**
 * 将大批量操作拆分为多个 Keycloak 事务批次执行的工具类。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
class BatchTaskRunner {

    /**
     * 按批次在独立事务中执行任务。
     *
     * @param first          起始偏移
     * @param count          总数量
     * @param batchCount     每批大小
     * @param sessionFactory 会话工厂
     * @param batchTask      批次任务回调
     */
    static void runInBatches(int first, int count, int batchCount, KeycloakSessionFactory sessionFactory, BatchTask batchTask) {

        final StateHolder state = new StateHolder();
        state.firstInThisBatch = first;
        state.remaining = count;
        state.countInThisBatch = Math.min(batchCount, state.remaining);
        while (state.remaining > 0) {
            KeycloakModelUtils.runJobInTransaction(sessionFactory, new KeycloakSessionTask() {

                @Override
                public void run(KeycloakSession session) {
                    batchTask.run(session, state.firstInThisBatch, state.countInThisBatch);
                }
            });

            // 更新下一批次的偏移与剩余数量
            state.firstInThisBatch = state.firstInThisBatch + state.countInThisBatch;
            state.remaining = state.remaining - state.countInThisBatch;
            state.countInThisBatch = Math.min(batchCount, state.remaining);
        }
    }


    /** 批次迭代状态。 */
    private static class StateHolder {
        /** 当前批次起始索引。 */
        int firstInThisBatch;
        /** 当前批次数量。 */
        int countInThisBatch;
        /** 剩余待处理数量。 */
        int remaining;
    };


    /** 单批次任务函数式接口。 */
    @FunctionalInterface
    public interface BatchTask {

        /** 执行一个批次内的操作。 */
        void run(KeycloakSession session, int firstInThisIteration, int countInThisIteration);

    }

}
