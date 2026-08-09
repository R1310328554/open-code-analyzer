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

package org.apache.rocketmq.store;

import org.apache.rocketmq.common.constant.LoggerName;
import org.apache.rocketmq.logging.org.slf4j.Logger;
import org.apache.rocketmq.logging.org.slf4j.LoggerFactory;

/**
 * MessageStore 生命周期状态机：加载、恢复、运行与关闭各阶段单向流转。
 */
public class MessageStoreStateMachine {
    /** 状态变更日志记录器。 */
    protected final Logger log;

    /** 当前状态。 */
    private MessageStoreState currentState;
    /** 上次状态变更时间戳。 */
    private long lastStateChangeTimestamp;
    /** 状态机启动时间戳。 */
    private final long startTimestamp;

    /** MessageStore 各生命周期阶段，order 数值越大表示越靠后。 */
    public enum MessageStoreState {
        /** 初始状态。 */
        INIT(0),

        /** 开始加载存储文件。 */
        LOAD_BEGIN(10),
        /** CommitLog 加载完成。 */
        LOAD_COMMITLOG_OK(11),
        /** ConsumeQueue 加载完成。 */
        LOAD_CONSUME_QUEUE_OK(12),
        /** 压缩索引加载完成。 */
        LOAD_COMPACTION_OK(13),
        /** 索引文件加载完成。 */
        LOAD_INDEX_OK(14),

        /** 开始恢复。 */
        RECOVER_BEGIN(20),
        /** ConsumeQueue 恢复完成。 */
        RECOVER_CONSUME_QUEUE_OK(21),
        /** CommitLog 恢复完成。 */
        RECOVER_COMMITLOG_OK(22),
        /** Topic-Queue 映射表恢复完成。 */
        RECOVER_TOPIC_QUEUE_TABLE_OK(23),

        /** 正常运行，可读写。 */
        RUNNING(30),

        /** 开始关闭。 */
        SHUTDOWN_BEGIN(40),
        /** 关闭完成。 */
        SHUTDOWN_OK(41);

        final int order;

        MessageStoreState(int order) {
            this.order = order;
        }

        /** 返回状态顺序值。 */
        public int getOrder() {
            return order;
        }

        /** 是否早于给定状态。 */
        public boolean isBefore(MessageStoreState storeState) {
            return this.order < storeState.order;
        }

        /** 是否晚于给定状态。 */
        public boolean isAfter(MessageStoreState storeState) {
            return this.order > storeState.order;
        }
    }


    /** 构造状态机，初始为 INIT 并记录启动时间。 */
    public MessageStoreStateMachine(Logger log) {
        this.log = log == null ? LoggerFactory.getLogger(LoggerName.STORE_LOGGER_NAME) : log;
        this.currentState = MessageStoreState.INIT;
        this.startTimestamp = System.currentTimeMillis();
        this.lastStateChangeTimestamp = startTimestamp;
        logStateChange(null, currentState, true);
    }

    /** 迁移到新状态（默认成功）。 */
    public void transitTo(MessageStoreState newState) {
        transitTo(newState, true);
    }

    /** 迁移到新状态，success 为 false 时仅记日志不更新 currentState。 */
    public void transitTo(MessageStoreState newState, boolean success) {
        if (!newState.isAfter(currentState)) {
            throw new IllegalStateException(
                String.format("Invalid state transition from %s to %s. Can only move forward.",
                    currentState, newState)
            );
        }

        logStateChange(currentState, newState, success);
        if (success) {
            this.currentState = newState;
            this.lastStateChangeTimestamp = System.currentTimeMillis();
        }
    }

    private void logStateChange(MessageStoreState fromState, MessageStoreState toState, boolean success) {
        if (fromState == null && success) {
            log.info("MessageStoreState initialized, state={}", toState);
        } else if (success) {
            log.info("MessageStoreState transition from {} to {}; Time in previous state={}ms, Total time={}ms",
                fromState, toState, getCurrentStateRunningTimeMs(), getTotalRunningTimeMs());
        } else {
            log.warn("MessageStoreState transition from {} to {} failed; Time in previous state={}ms, Total "
                + "time={}ms", fromState, toState, getCurrentStateRunningTimeMs(), getTotalRunningTimeMs());
        }
    }

    /** 返回当前状态。 */
    public MessageStoreState getCurrentState() {
        return currentState;
    }

    /** 自启动以来的总运行毫秒数。 */
    public long getTotalRunningTimeMs() {
        return System.currentTimeMillis() - startTimestamp;
    }

    /** 当前状态已持续毫秒数。 */
    public long getCurrentStateRunningTimeMs() {
        return System.currentTimeMillis() - lastStateChangeTimestamp;
    }
}
