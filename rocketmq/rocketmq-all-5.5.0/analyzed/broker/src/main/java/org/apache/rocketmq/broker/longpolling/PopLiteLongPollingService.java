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

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.common.ServiceThread;
import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.netty.NettyRemotingAbstract;
import org.apache.rocketmq.remoting.netty.NettyRequestProcessor;
import org.apache.rocketmq.remoting.netty.RequestTask;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;

import static org.apache.rocketmq.broker.longpolling.PollingResult.NOT_POLLING;
import static org.apache.rocketmq.broker.longpolling.PollingResult.POLLING_FULL;
import static org.apache.rocketmq.broker.longpolling.PollingResult.POLLING_SUC;
import static org.apache.rocketmq.broker.longpolling.PollingResult.POLLING_TIMEOUT;

/**
 * Lite 消费专用长轮询服务：以 clientId 为键在内存中挂起 POP 请求（而非 topic@cid@qid）。
 * 消息到达通知与资源清理机制与 {@link PopLongPollingService} 一致。
 */
public class PopLiteLongPollingService extends ServiceThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerName.ROCKETMQ_POP_LITE_LOGGER_NAME);

    private final BrokerController brokerController;
    private final NettyRequestProcessor processor;
    private final ConcurrentLinkedHashMap<String, ConcurrentSkipListSet<PopRequest>> pollingMap;
    private long lastCleanTime = 0;

    private final AtomicLong totalPollingNum = new AtomicLong(0);
    private final boolean notifyLast;

    /** 初始化 lite 轮询映射，{@code notifyLast} 控制唤醒时取队首还是队尾请求。 */
    public PopLiteLongPollingService(BrokerController brokerController, NettyRequestProcessor processor, boolean notifyLast) {
        this.brokerController = brokerController;
        this.processor = processor;
        this.pollingMap = new ConcurrentLinkedHashMap.Builder<String, ConcurrentSkipListSet<PopRequest>>()
            .maximumWeightedCapacity(this.brokerController.getBrokerConfig().getPopPollingMapSize()).build();
        this.notifyLast = notifyLast;
    }

    /** 容器模式下附加 broker 标识前缀。 */
    @Override
    public String getServiceName() {
        if (brokerController.getBrokerConfig().isInBrokerContainer()) {
            return brokerController.getBrokerIdentity().getIdentifier() + PopLiteLongPollingService.class.getSimpleName();
        }
        return PopLiteLongPollingService.class.getSimpleName();
    }

    /** 周期性扫描超时挂起请求、统计队列深度并清理空桶。 */
    @Override
    public void run() {
        int i = 0;
        while (!this.stopped) {
            try {
                this.waitForRunning(20);
                i++;
                if (pollingMap.isEmpty()) {
                    continue;
                }
                long tmpTotalPollingNum = 0;
                for (Map.Entry<String, ConcurrentSkipListSet<PopRequest>> entry : pollingMap.entrySet()) {
                    String key = entry.getKey();
                    ConcurrentSkipListSet<PopRequest> popQ = entry.getValue();
                    if (popQ == null) {
                        continue;
                    }
                    PopRequest first;
                    do {
                        first = popQ.pollFirst();
                        if (first == null) {
                            break;
                        }
                        if (!first.isTimeout()) {
                            if (popQ.add(first)) {
                                break;
                            } else {
                                LOGGER.info("lite polling, add back again but failed. {}", first);
                            }
                        }
                        if (brokerController.getBrokerConfig().isEnablePopLog()) {
                            LOGGER.info("timeout , wakeUp lite polling : {}", first);
                        }
                        totalPollingNum.decrementAndGet();
                        wakeUp(first);
                    }
                    while (true);
                    if (i >= 100) {
                        long tmpPollingNum = popQ.size();
                        tmpTotalPollingNum = tmpTotalPollingNum + tmpPollingNum;
                        if (tmpPollingNum > 20) {
                            LOGGER.info("lite polling queue {} , size={} ", key, tmpPollingNum);
                        }
                    }
                }

                if (i >= 100) {
                    LOGGER.info("litePollingMapSize={}, tmpTotalSize={}, atomicTotalSize={}, diffSize={}",
                        pollingMap.size(), tmpTotalPollingNum, totalPollingNum.get(),
                        Math.abs(totalPollingNum.get() - tmpTotalPollingNum));
                    totalPollingNum.set(tmpTotalPollingNum);
                    i = 0;
                }

                // clean unused
                if (lastCleanTime == 0 || System.currentTimeMillis() - lastCleanTime > 3 * 60 * 1000) {
                    cleanUnusedResource();
                }
            } catch (Throwable e) {
                LOGGER.error("checkLitePolling error", e);
            }
        }
        // clean all;
        try {
            for (Map.Entry<String, ConcurrentSkipListSet<PopRequest>> entry : pollingMap.entrySet()) {
                ConcurrentSkipListSet<PopRequest> popQ = entry.getValue();
                PopRequest first;
                while ((first = popQ.pollFirst()) != null) {
                    wakeUp(first);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /** 新消息到达时按 clientId 唤醒对应挂起 POP 请求。 */
    public boolean notifyMessageArriving(final String clientId, boolean force, long msgStoreTime, String group) {
        String pollingKey = getPollingKey(clientId, group);
        ConcurrentSkipListSet<PopRequest> remotingCommands = pollingMap.get(pollingKey);
        if (remotingCommands == null || remotingCommands.isEmpty()) {
            return false;
        }
        PopRequest popRequest = pollRemotingCommands(remotingCommands);
        if (popRequest == null) {
            return false;
        }

        if (brokerController.getBrokerConfig().isEnableLitePopLog()) {
            LOGGER.info("notify lite polling, wakeUp: {}", popRequest);
        }
        return wakeUp(popRequest);
    }

    /** 完成挂起请求并在 pull 线程池中重新执行 POP 处理逻辑。 */
    public boolean wakeUp(final PopRequest request) {
        if (request == null || !request.complete()) {
            return false;
        }
        if (!request.getCtx().channel().isActive()) {
            return false;
        }

        Runnable run = () -> {
            try {
                final RemotingCommand response = processor.processRequest(request.getCtx(), request.getRemotingCommand());
                if (response != null) {
                    response.setOpaque(request.getRemotingCommand().getOpaque());
                    response.markResponseType();
                    NettyRemotingAbstract.writeResponse(request.getChannel(), request.getRemotingCommand(), response, future -> {
                        if (!future.isSuccess()) {
                            LOGGER.error("ProcessRequestWrapper response to {} failed", request.getChannel().remoteAddress(), future.cause());
                            LOGGER.error(request.toString());
                            LOGGER.error(response.toString());
                        }
                    }, brokerController.getBrokerMetricsManager().getRemotingMetricsManager());
                }
            } catch (Exception e) {
                LOGGER.error("ExecuteRequestWhenWakeup error.", e);
            }
        };

        this.brokerController.getPullMessageExecutor().submit(
            new RequestTask(run, request.getChannel(), request.getRemotingCommand()));
        return true;
    }

    /** 尝试将 POP 请求挂入 lite 轮询队列，返回挂起结果。 */
    public PollingResult polling(final ChannelHandlerContext ctx, RemotingCommand remotingCommand,
        long bornTime, long pollTime, String clientId, String group) {
        if (pollTime <= 0 || this.isStopped()) {
            return NOT_POLLING;
        }
        long expired = bornTime + pollTime;
        final PopRequest request = new PopRequest(remotingCommand, ctx, expired, null, null);
        boolean isFull = totalPollingNum.get() >= this.brokerController.getBrokerConfig().getMaxPopPollingSize();
        if (isFull) {
            LOGGER.info("lite polling {}, result POLLING_FULL, total:{}", remotingCommand, totalPollingNum.get());
            return POLLING_FULL;
        }
        boolean isTimeout = request.isTimeout();
        if (isTimeout) {
            if (brokerController.getBrokerConfig().isEnablePopLog()) {
                LOGGER.info("lite polling {}, result POLLING_TIMEOUT", remotingCommand);
            }
            return POLLING_TIMEOUT;
        }

        String pollingKey = getPollingKey(clientId, group);
        ConcurrentSkipListSet<PopRequest> queue = pollingMap.get(pollingKey);
        if (queue == null) {
            queue = new ConcurrentSkipListSet<>(PopRequest.COMPARATOR);
            ConcurrentSkipListSet<PopRequest> old = pollingMap.putIfAbsent(pollingKey, queue);
            if (old != null) {
                queue = old;
            }
        } else {
            // check size
            int size = queue.size();
            if (size > brokerController.getBrokerConfig().getPopPollingSize()) {
                LOGGER.info("lite polling {}, result POLLING_FULL, singleSize:{}", remotingCommand, size);
                return POLLING_FULL;
            }
        }
        if (queue.add(request)) {
            remotingCommand.setSuspended(true);
            totalPollingNum.incrementAndGet();
            if (brokerController.getBrokerConfig().isEnableLitePopLog()) {
                LOGGER.info("lite polling {}, result POLLING_SUC", remotingCommand);
            }
            return POLLING_SUC;
        } else {
            LOGGER.info("lite polling {}, result POLLING_FULL, add fail, {}", request, queue);
            return POLLING_FULL;
        }
    }

    /** 每 3 分钟移除空的轮询桶以释放内存。 */
    private void cleanUnusedResource() {
        try {
            pollingMap.entrySet().removeIf(entry -> {
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    LOGGER.info("clean polling structure of {}", entry.getKey()); // see getPollingKey()
                    return true;
                }
                return false;
            });
        } catch (Throwable ignored) {
        }
        lastCleanTime = System.currentTimeMillis();
    }

    /** 从挂起集合中取出首个活跃连接对应的请求。 */
    private PopRequest pollRemotingCommands(ConcurrentSkipListSet<PopRequest> remotingCommands) {
        if (remotingCommands == null || remotingCommands.isEmpty()) {
            return null;
        }

        PopRequest popRequest;
        do {
            if (notifyLast) {
                popRequest = remotingCommands.pollLast();
            } else {
                popRequest = remotingCommands.pollFirst();
            }
            if (popRequest != null) {
                totalPollingNum.decrementAndGet();
            }
        } while (popRequest != null && !popRequest.getChannel().isActive());

        return popRequest;
    }

    /** lite 模式下假定 clientId 全局唯一，直接作为轮询桶键。 */
    private String getPollingKey(String clientId, String group) {
        return clientId;
    }
}
