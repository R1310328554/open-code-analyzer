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

package org.apache.rocketmq.namesrv.routeinfo;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.common.namesrv.NamesrvConfig;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.protocol.header.namesrv.UnRegisterBrokerRequestHeader;

/**
 * Broker 批量注销服务：将注销请求入队并批量处理，加速 Broker 下线时的路由清理。
 */
public class BatchUnregistrationService extends ServiceThread {
    /** 路由信息管理器，执行实际注销逻辑。 */
    private final RouteInfoManager routeInfoManager;
    /** 待处理的 Broker 注销请求队列。 */
    private BlockingQueue<UnRegisterBrokerRequestHeader> unregistrationQueue;
    private static final Logger log = LoggerFactory.getLogger(LoggerName.NAMESRV_LOGGER_NAME);

    /** 构造服务并初始化有界注销队列。 */
    public BatchUnregistrationService(RouteInfoManager routeInfoManager, NamesrvConfig namesrvConfig) {
        this.routeInfoManager = routeInfoManager;
        this.unregistrationQueue = new LinkedBlockingQueue<>(namesrvConfig.getUnRegisterBrokerQueueCapacity());
    }

    /**
     * 提交一条 Broker 注销请求到队列。
     *
     * @param unRegisterRequest 待提交的注销请求
     * @return {@code true} 表示入队成功，{@code false} 表示队列已满
     */
    public boolean submit(UnRegisterBrokerRequestHeader unRegisterRequest) {
        return unregistrationQueue.offer(unRegisterRequest);
    }

    @Override
    /** 返回服务线程名称标识。 */
    public String getServiceName() {
        return BatchUnregistrationService.class.getName();
    }

    @Override
    /** 后台循环：阻塞取请求并 drain 同批请求后批量注销。 */
    public void run() {
        while (!this.isStopped()) {
            try {
                final UnRegisterBrokerRequestHeader request = unregistrationQueue.take();
                Set<UnRegisterBrokerRequestHeader> unregistrationRequests = new HashSet<>();
                unregistrationQueue.drainTo(unregistrationRequests);

                // 将本次 poll 到的请求一并纳入批量处理
                unregistrationRequests.add(request);

                this.routeInfoManager.unRegisterBroker(unregistrationRequests);
            } catch (Throwable e) {
                log.error("Handle unregister broker request failed", e);
            }
        }
    }

    // 仅供测试：返回当前队列长度
    int queueLength() {
        return this.unregistrationQueue.size();
    }
}
