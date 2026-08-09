/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rocketmq.broker.longpolling;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.apache.rocketmq.broker.metrics.ConsumerLagCalculator;
import org.apache.rocketmq.remoting.CommandCallback;

/**
 * POP 命令回调：在 Remoting 异步响应完成后触发消费滞后计算。
 */
public class PopCommandCallback implements CommandCallback {

    private final BiConsumer<ConsumerLagCalculator.ProcessGroupInfo,
        CompletableFuture<ConsumerLagCalculator.CalculateLagResult>> biConsumer;
    private final ConsumerLagCalculator.ProcessGroupInfo info;
    private final CompletableFuture<ConsumerLagCalculator.CalculateLagResult> future;

    /** 绑定滞后计算回调、进程组上下文与结果 Future。 */
    public PopCommandCallback(
        BiConsumer<ConsumerLagCalculator.ProcessGroupInfo,
            CompletableFuture<ConsumerLagCalculator.CalculateLagResult>> biConsumer,
        ConsumerLagCalculator.ProcessGroupInfo info,
        CompletableFuture<ConsumerLagCalculator.CalculateLagResult> future) {

        this.biConsumer = biConsumer;
        this.info = info;
        this.future = future;
    }

    /** 执行滞后计算并将结果写入 Future。 */
    @Override
    public void accept() {
        biConsumer.accept(info, future);
    }
}
