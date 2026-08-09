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

package org.apache.rocketmq.common.thread;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池队列长度监控：队列超过容量 85% 时触发 jstack 打印。
 */
public class ThreadPoolQueueSizeMonitor implements ThreadPoolStatusMonitor {

    /** 队列最大容量，用于计算 85% 告警阈值。 */
    private final int maxQueueCapacity;

    /** 指定队列容量上限构造监控器。 */
    public ThreadPoolQueueSizeMonitor(int maxQueueCapacity) {
        this.maxQueueCapacity = maxQueueCapacity;
    }

    /** 监控指标名称：queueSize。 */
    @Override
    public String describe() {
        return "queueSize";
    }

    /** 返回当前队列待执行任务数。 */
    @Override
    public double value(ThreadPoolExecutor executor) {
        return executor.getQueue().size();
    }

    /** 队列长度超过容量 85% 时需要打印 jstack。 */
    @Override
    public boolean needPrintJstack(ThreadPoolExecutor executor, double value) {
        return value > maxQueueCapacity * 0.85;
    }
}
