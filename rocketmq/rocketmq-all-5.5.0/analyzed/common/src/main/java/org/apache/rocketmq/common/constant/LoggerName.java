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
package org.apache.rocketmq.common.constant;

/**
 * RocketMQ 各模块 SLF4J Logger 名称常量。
 */
public class LoggerName {
    /** Filter Server 日志名。 */
    public static final String FILTERSRV_LOGGER_NAME = "RocketmqFiltersrv";
    /** NameServer 日志名。 */
    public static final String NAMESRV_LOGGER_NAME = "RocketmqNamesrv";
    /** NameServer 控制台日志名。 */
    public static final String NAMESRV_CONSOLE_LOGGER_NAME = "RocketmqNamesrvConsole";
    /** Controller 日志名。 */
    public static final String CONTROLLER_LOGGER_NAME = "RocketmqController";
    /** Controller 控制台日志名。 */
    public static final String CONTROLLER_CONSOLE_NAME = "RocketmqControllerConsole";
    /** NameServer 水位监控日志名。 */
    public static final String NAMESRV_WATER_MARK_LOGGER_NAME = "RocketmqNamesrvWaterMark";
    /** Broker 日志名。 */
    public static final String BROKER_LOGGER_NAME = "RocketmqBroker";
    /** Broker 控制台日志名。 */
    public static final String BROKER_CONSOLE_NAME = "RocketmqConsole";
    /** Client 日志名。 */
    public static final String CLIENT_LOGGER_NAME = "RocketmqClient";
    /** 流量统计日志名。 */
    public static final String ROCKETMQ_TRAFFIC_NAME = "RocketmqTraffic";
    /** Remoting 网络层日志名。 */
    public static final String ROCKETMQ_REMOTING_NAME = "RocketmqRemoting";
    /** 命令行工具日志名。 */
    public static final String TOOLS_LOGGER_NAME = "RocketmqTools";
    /** Common 公共模块日志名。 */
    public static final String COMMON_LOGGER_NAME = "RocketmqCommon";
    /** Store 存储层日志名。 */
    public static final String STORE_LOGGER_NAME = "RocketmqStore";
    /** Store 错误日志名。 */
    public static final String STORE_ERROR_LOGGER_NAME = "RocketmqStoreError";
    /** 事务消息日志名。 */
    public static final String TRANSACTION_LOGGER_NAME = "RocketmqTransaction";
    /** Rebalance 锁日志名。 */
    public static final String REBALANCE_LOCK_LOGGER_NAME = "RocketmqRebalanceLock";
    /** 统计指标日志名。 */
    public static final String ROCKETMQ_STATS_LOGGER_NAME = "RocketmqStats";
    /** 死信队列统计日志名。 */
    public static final String DLQ_STATS_LOGGER_NAME = "RocketmqDLQStats";
    /** 死信队列日志名。 */
    public static final String DLQ_LOGGER_NAME = "RocketmqDLQ";
    /** 消费者统计日志名。 */
    public static final String CONSUMER_STATS_LOGGER_NAME = "RocketmqConsumerStats";
    /** 商业化/计费日志名。 */
    public static final String COMMERCIAL_LOGGER_NAME = "RocketmqCommercial";
    /** 账户相关日志名。 */
    public static final String ACCOUNT_LOGGER_NAME = "RocketmqAccount";
    /** 流控日志名。 */
    public static final String FLOW_CONTROL_LOGGER_NAME = "RocketmqFlowControl";
    /** 鉴权日志名。 */
    public static final String ROCKETMQ_AUTHORIZE_LOGGER_NAME = "RocketmqAuthorize";
    /** 消息去重日志名。 */
    public static final String DUPLICATION_LOGGER_NAME = "RocketmqDuplication";
    /** 保护/限流日志名。 */
    public static final String PROTECTION_LOGGER_NAME = "RocketmqProtection";
    /** 水位监控日志名。 */
    public static final String WATER_MARK_LOGGER_NAME = "RocketmqWaterMark";
    /** 消息过滤日志名。 */
    public static final String FILTER_LOGGER_NAME = "RocketmqFilter";
    /** POP 消费模式日志名。 */
    public static final String ROCKETMQ_POP_LOGGER_NAME = "RocketmqPop";
    /** POP Lite 消费模式日志名。 */
    public static final String ROCKETMQ_POP_LITE_LOGGER_NAME = "RocketmqPopLite";
    /** 故障转移日志名。 */
    public static final String FAILOVER_LOGGER_NAME = "RocketmqFailover";
    /** 标准输出 Logger 名。 */
    public static final String STDOUT_LOGGER_NAME = "STDOUT";
    /** Proxy 代理日志名。 */
    public static final String PROXY_LOGGER_NAME = "RocketmqProxy";
    /** Proxy 水位监控日志名。 */
    public static final String PROXY_WATER_MARK_LOGGER_NAME = "RocketmqProxyWatermark";
    /** 冷读控制日志名。 */
    public static final String ROCKETMQ_COLDCTR_LOGGER_NAME = "RocketmqColdCtr";
    /** RocksDB 存储日志名。 */
    public static final String ROCKSDB_LOGGER_NAME = "RocketmqRocksDB";

    /** 鉴权审计日志名。 */
    public static final String ROCKETMQ_AUTH_AUDIT_LOGGER_NAME = "RocketmqAuthAudit";
}
