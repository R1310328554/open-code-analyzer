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

package com.alibaba.nacos.api.remote;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RPC 模块专用守护线程定时调度器。
 *
 * <p>继承 {@link ScheduledThreadPoolExecutor}，创建 daemon 线程；提供超时检测、流控延迟与通用服务端调度三类单线程池。</p>
 *
 * @author liuzunfei
 * @version $Id: RpcScheduledExecutor.java, v 0.1 2020年09月07日 4:12 PM liuzunfei Exp $
 */
public class RpcScheduledExecutor extends ScheduledThreadPoolExecutor {
    
    /** 请求超时检测调度器（单线程）。 */
    public static final RpcScheduledExecutor TIMEOUT_SCHEDULER = new RpcScheduledExecutor(1,
        "com.alibaba.nacos.remote.TimerScheduler");
    
    /** 流控延迟任务调度器（单线程）。 */
    public static final RpcScheduledExecutor CONTROL_SCHEDULER = new RpcScheduledExecutor(1,
        "com.alibaba.nacos.control.DelayScheduler");
    
    /** 服务端通用定时任务调度器（单线程）。 */
    public static final RpcScheduledExecutor COMMON_SERVER_EXECUTOR = new RpcScheduledExecutor(1,
        "com.alibaba.nacos.remote.ServerCommonScheduler");
    
    /**
     * 创建守护线程定时线程池。
     *
     * @param corePoolSize 核心线程数
     * @param threadName   线程名前缀
     */
        super(corePoolSize, new ThreadFactory() {
            
            /** 线程序号计数器。 */
            private final AtomicLong index = new AtomicLong();
            
            /** 创建带递增后缀的 daemon 线程。 */
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, threadName + "." + index.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        });
    }
    
}
