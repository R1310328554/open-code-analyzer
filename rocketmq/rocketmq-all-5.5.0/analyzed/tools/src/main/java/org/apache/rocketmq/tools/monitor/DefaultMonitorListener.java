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

package org.apache.rocketmq.tools.monitor;

import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.remoting.protocol.body.ConsumerRunningInfo;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * {@link MonitorListener} 的默认实现：将监控事件输出到 SLF4J 日志。
 * <p>适用于命令行或独立进程中的轻量级监控场景。
 */
public class DefaultMonitorListener implements MonitorListener {
    /** 监控日志统一前缀。 */
    private final static String LOG_PREFIX = "[MONITOR] ";
    /** 需运维关注的告警类日志前缀。 */
    private final static String LOG_NOTIFY = LOG_PREFIX + " [NOTIFY] ";
    /** 本监听器使用的日志记录器。 */
    private final Logger logger = LoggerFactory.getLogger(DefaultMonitorListener.class);

    /** 使用默认日志配置构造监听器。 */
    public DefaultMonitorListener() {
    }

    /** 一轮监控开始时打印分隔日志。 */
    @Override
    public void beginRound() {
        logger.info("{}=========================================beginRound", LOG_PREFIX);
    }

    /** 记录未消费消息积压统计。 */
    @Override
    public void reportUndoneMsgs(UndoneMsgs undoneMsgs) {
        logger.info("{}reportUndoneMsgs: {}", LOG_PREFIX, undoneMsgs);
    }

    /** 记录近期消费失败消息汇总。 */
    @Override
    public void reportFailedMsgs(FailedMsgs failedMsgs) {
        logger.info("{}reportFailedMsgs: {}", LOG_PREFIX, failedMsgs);
    }

    /** 记录 Broker 偏移量迁移导致的消息删除事件。 */
    @Override
    public void reportDeleteMsgsEvent(DeleteMsgsEvent deleteMsgsEvent) {
        logger.info("{}reportDeleteMsgsEvent: {}", LOG_PREFIX, deleteMsgsEvent);
    }

    /** 分析并记录消费者运行态：订阅一致性与队列堆积。 */
    @Override
    public void reportConsumerRunningInfo(TreeMap<String, ConsumerRunningInfo> criTable) {
        // 无运行态数据时告警
        if (criTable == null || criTable.isEmpty()) {
            logger.warn("{}ConsumerRunningInfo is empty.", LOG_NOTIFY);
            return;
        }

        ConsumerRunningInfo firstValue = criTable.firstEntry().getValue();
        if (firstValue == null || firstValue.getProperties() == null) {
            logger.warn("{}ConsumerRunningInfo entry is empty.", LOG_NOTIFY);
            return;
        }

        String consumerGroup = firstValue.getProperties().getProperty("consumerGroup");

        {
            // 校验同组各客户端订阅是否一致
            boolean result = ConsumerRunningInfo.analyzeSubscription(criTable);
            if (!result) {
                logger.info("{}reportConsumerRunningInfo: ConsumerGroup: {}, Subscription different", LOG_NOTIFY, consumerGroup);
            }
        }

        {
            // 逐客户端分析 ProcessQueue 堆积与延迟
            Iterator<Entry<String, ConsumerRunningInfo>> it = criTable.entrySet().iterator();
            while (it.hasNext()) {
                Entry<String, ConsumerRunningInfo> next = it.next();
                String result = ConsumerRunningInfo.analyzeProcessQueue(next.getKey(), next.getValue());
                if (!result.isEmpty()) {
                    logger.info("{}reportConsumerRunningInfo: ConsumerGroup: {}, ClientId: {}, {}",
                            LOG_NOTIFY,
                            consumerGroup,
                            next.getKey(),
                            result);
                }
            }
        }
    }

    /** 一轮监控结束时打印分隔日志。 */
    @Override
    public void endRound() {
        logger.info("{}=========================================endRound", LOG_PREFIX);
    }
}
