/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.utils;

import com.alibaba.nacos.common.utils.NumberUtils;

/**
 * 远程 RPC 线程池配置工具：读取系统属性覆盖默认线程倍数与队列容量。
 * util of remote.
 *
 * @author liuzunfei
 * @version $Id: RemoteUtils.java, v 0.1 2020年11月12日 8:54 PM liuzunfei Exp $
 */
public class RemoteUtils {
    
    /** 远程模块类加载相关因子（预留常量）。 */
    public static final float LOADER_FACTOR = 0.1f;
    
    /** 默认远程执行线程数 = CPU 核数 × 16。 */

    private static final int REMOTE_EXECUTOR_TIMES_OF_PROCESSORS = 1 << 4;
    
    /** 默认远程执行队列容量：16384（2^14）。 */

    private static final int REMOTE_EXECUTOR_QUEUE_SIZE = 1 << 14;
    
    /**
     * 获取远程执行线程相对 CPU 核数的倍数，可通过 {@code remote.executor.times.of.processors} 覆盖。
     * get remote executors thread times of processors,default is 16. see the usage of this method for detail.
     *
     * @return times of processors.
     */
    public static int getRemoteExecutorTimesOfProcessors() {
        String timesString = System.getProperty("remote.executor.times.of.processors");
        if (NumberUtils.isDigits(timesString)) {
            int times = Integer.parseInt(timesString);
            return times > 0 ? times : REMOTE_EXECUTOR_TIMES_OF_PROCESSORS;
        } else {
            return REMOTE_EXECUTOR_TIMES_OF_PROCESSORS;
        }
    }
    
    /** 获取远程执行队列大小，可通过 {@code remote.executor.queue.size} 覆盖。 */
    public static int getRemoteExecutorQueueSize() {
        String queueSizeString = System.getProperty("remote.executor.queue.size");
        if (NumberUtils.isDigits(queueSizeString)) {
            int size = Integer.parseInt(queueSizeString);
            return size > 0 ? size : REMOTE_EXECUTOR_QUEUE_SIZE;
        } else {
            return REMOTE_EXECUTOR_QUEUE_SIZE;
        }
    }
}
