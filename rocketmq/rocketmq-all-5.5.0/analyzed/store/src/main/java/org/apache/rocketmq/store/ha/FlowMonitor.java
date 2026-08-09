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

package org.apache.rocketmq.store.ha;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.store.config.MessageStoreConfig;

/**
 * HA 传输流量监视器：每秒统计传输字节并支持流控配额计算。
 */
public class FlowMonitor extends ServiceThread {
    /** 当前秒内累计传输字节数。 */
    private final AtomicLong transferredByte = new AtomicLong(0L);
    /** 上一秒完成的传输字节数快照。 */
    private volatile long transferredByteInSecond;
    /** 消息存储配置，含 HA 流控开关与上限。 */
    protected MessageStoreConfig messageStoreConfig;

    /** 注入 MessageStoreConfig。 */
    public FlowMonitor(MessageStoreConfig messageStoreConfig) {
        this.messageStoreConfig = messageStoreConfig;
    }

    /** 每秒重置计数并计算传输速率。 */
    @Override
    public void run() {
        while (!this.isStopped()) {
            this.waitForRunning(1 * 1000);
            this.calculateSpeed();
        }
    }

    /** 将累计值写入快照并清零计数器。 */
    public void calculateSpeed() {
        this.transferredByteInSecond = this.transferredByte.get();
        this.transferredByte.set(0);
    }

    /** 返回本周期尚可传输的最大字节数。 */
    public int canTransferMaxByteNum() {
        // 当前若启用流控则按配额计算本周期可传字节数
        if (this.isFlowControlEnable()) {
            long res = Math.max(this.maxTransferByteInSecond() - this.transferredByte.get(), 0);
            return res > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) res;
        }
        return Integer.MAX_VALUE;
    }

    /** 累加已传输字节数。 */
    public void addByteCountTransferred(long count) {
        this.transferredByte.addAndGet(count);
    }

    /** 返回上一秒传输字节数。 */
    public long getTransferredByteInSecond() {
        return this.transferredByteInSecond;
    }

    /** 返回服务线程名称。 */
    @Override
    public String getServiceName() {
        return FlowMonitor.class.getSimpleName();
    }

    /** 是否启用 HA 流控。 */
    protected boolean isFlowControlEnable() {
        return this.messageStoreConfig.isHaFlowControlEnable();
    }

    /** 返回每秒最大可传输字节配置值。 */
    public long maxTransferByteInSecond() {
        return this.messageStoreConfig.getMaxHaTransferByteInSecond();
    }
}