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
package org.apache.rocketmq.client.impl.consumer;

import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * Rebalance 后台服务：周期性触发 {@link MQClientInstance#doRebalance()}，
 * 协调消费组内队列分配与负载均衡。
 */
public class RebalanceService extends ServiceThread {
    /** rebalance 已均衡时的等待间隔（毫秒）。 */
    private static long waitInterval =
        Long.parseLong(System.getProperty(
            "rocketmq.client.rebalance.waitInterval", "20000"));
    /** rebalance 未均衡时的最小重试间隔（毫秒）。 */
    private static long minInterval =
        Long.parseLong(System.getProperty(
            "rocketmq.client.rebalance.minInterval", "1000"));
    private final Logger log = LoggerFactory.getLogger(RebalanceService.class);
    /** 关联的客户端工厂。 */
    private final MQClientInstance mqClientFactory;
    /** 上次 rebalance 时间戳。 */
    private long lastRebalanceTimestamp = System.currentTimeMillis();

    /** 构造 rebalance 服务。 */
    public RebalanceService(MQClientInstance mqClientFactory) {
        this.mqClientFactory = mqClientFactory;
    }

    @Override
    /** 主循环：按间隔触发 rebalance，未均衡时缩短等待。 */
    public void run() {
        log.info(this.getServiceName() + " service started");

        long realWaitInterval = waitInterval;
        while (!this.isStopped()) {
            this.waitForRunning(realWaitInterval);

            long interval = System.currentTimeMillis() - lastRebalanceTimestamp;
            if (interval < minInterval) {
                realWaitInterval = minInterval - interval;
            } else {
                boolean balanced = this.mqClientFactory.doRebalance();
                realWaitInterval = balanced ? waitInterval : minInterval;
                lastRebalanceTimestamp = System.currentTimeMillis();
            }
        }

        log.info(this.getServiceName() + " service end");
    }

    @Override
    public String getServiceName() {
        return RebalanceService.class.getSimpleName();
    }
}
