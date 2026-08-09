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
package org.apache.rocketmq.store.stats;

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;
import org.apache.rocketmq.store.MessageStore;

/**
 * Broker 级消息收发日统计：记录昨日与今日累计 put/get 数量。
 */
public class BrokerStats {
    /** 存储模块日志。 */
    private static final Logger log = LoggerFactory.getLogger(LoggerName.BROKER_LOGGER_NAME);

    /** 关联的 MessageStore。 */
    private final MessageStore defaultMessageStore;

    /** 昨日早晨 put 累计快照。 */
    private volatile long msgPutTotalYesterdayMorning;

    /** 今日早晨 put 累计快照。 */
    private volatile long msgPutTotalTodayMorning;

    /** 昨日早晨 get 累计快照。 */
    private volatile long msgGetTotalYesterdayMorning;

    /** 今日早晨 get 累计快照。 */
    private volatile long msgGetTotalTodayMorning;

        /** @param defaultMessageStore 消息存储实例 */
    public BrokerStats(MessageStore defaultMessageStore) {
        this.defaultMessageStore = defaultMessageStore;
    }

    /** 滚动记录昨日/今日消息 put 与 get 累计值。 */
    public void record() {
        this.msgPutTotalYesterdayMorning = this.msgPutTotalTodayMorning;
        this.msgGetTotalYesterdayMorning = this.msgGetTotalTodayMorning;

        this.msgPutTotalTodayMorning =
            this.defaultMessageStore.getBrokerStatsManager().getBrokerPutNumsWithoutSystemTopic();
        this.msgGetTotalTodayMorning =
            this.defaultMessageStore.getBrokerStatsManager().getBrokerGetNumsWithoutSystemTopic();

        log.info("yesterday put message total: {}", msgPutTotalTodayMorning - msgPutTotalYesterdayMorning);
        log.info("yesterday get message total: {}", msgGetTotalTodayMorning - msgGetTotalYesterdayMorning);
    }

    /** 昨日早晨 put 累计快照。 */
    public long getMsgPutTotalYesterdayMorning() {
        return msgPutTotalYesterdayMorning;
    }

    /** 设置昨日早晨 put 累计快照。 */
    public void setMsgPutTotalYesterdayMorning(long msgPutTotalYesterdayMorning) {
        this.msgPutTotalYesterdayMorning = msgPutTotalYesterdayMorning;
    }

    /** 今日早晨 put 累计快照。 */
    public long getMsgPutTotalTodayMorning() {
        return msgPutTotalTodayMorning;
    }

    /** 设置今日早晨 put 累计快照。 */
    public void setMsgPutTotalTodayMorning(long msgPutTotalTodayMorning) {
        this.msgPutTotalTodayMorning = msgPutTotalTodayMorning;
    }

    /** 昨日早晨 get 累计快照。 */
    public long getMsgGetTotalYesterdayMorning() {
        return msgGetTotalYesterdayMorning;
    }

    /** 设置昨日早晨 get 累计快照。 */
    public void setMsgGetTotalYesterdayMorning(long msgGetTotalYesterdayMorning) {
        this.msgGetTotalYesterdayMorning = msgGetTotalYesterdayMorning;
    }

    /** 今日早晨 get 累计快照。 */
    public long getMsgGetTotalTodayMorning() {
        return msgGetTotalTodayMorning;
    }

    /** 设置今日早晨 get 累计快照。 */
    public void setMsgGetTotalTodayMorning(long msgGetTotalTodayMorning) {
        this.msgGetTotalTodayMorning = msgGetTotalTodayMorning;
    }

    /** 当前 put 累计（不含系统 Topic）。 */
    public long getMsgPutTotalTodayNow() {
        return this.defaultMessageStore.getBrokerStatsManager().getBrokerPutNumsWithoutSystemTopic();
    }

    /** 当前 get 累计（不含系统 Topic）。 */
    public long getMsgGetTotalTodayNow() {
        return this.defaultMessageStore.getBrokerStatsManager().getBrokerGetNumsWithoutSystemTopic();
    }
}
