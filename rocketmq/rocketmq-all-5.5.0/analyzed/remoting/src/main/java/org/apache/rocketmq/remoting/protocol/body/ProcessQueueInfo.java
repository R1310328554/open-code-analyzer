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

package org.apache.rocketmq.remoting.protocol.body;

import org.apache.rocketmq.common.UtilAll;

/**
 * Push/Pull 消费 ProcessQueue 运行态快照：缓存消息范围、事务消息、顺序锁及拉取/消费时间戳。
 */
public class ProcessQueueInfo {
    /** 已提交消费位点。 */
    private long commitOffset;

    /** 本地缓存消息最小逻辑位点。 */
    private long cachedMsgMinOffset;
    /** 本地缓存消息最大逻辑位点。 */
    private long cachedMsgMaxOffset;
    /** 本地缓存消息条数。 */
    private int cachedMsgCount;
    /** 本地缓存消息占用内存（MiB）。 */
    private int cachedMsgSizeInMiB;

    /** 事务消息最小位点。 */
    private long transactionMsgMinOffset;
    /** 事务消息最大位点。 */
    private long transactionMsgMaxOffset;
    /** 待处理事务消息条数。 */
    private int transactionMsgCount;

    /** 顺序消费队列是否已加锁。 */
    private boolean locked;
    /** 尝试解锁次数。 */
    private long tryUnlockTimes;
    /** 最近一次加锁时间戳。 */
    private long lastLockTimestamp;

    /** 队列是否已被丢弃（Rebalance 回收）。 */
    private boolean droped;
    /** 最近一次拉取时间戳。 */
    private long lastPullTimestamp;
    /** 最近一次消费时间戳。 */
    private long lastConsumeTimestamp;

    /** 返回已提交位点。 */
    public long getCommitOffset() {
        return commitOffset;
    }

    public void setCommitOffset(long commitOffset) {
        this.commitOffset = commitOffset;
    }

    public long getCachedMsgMinOffset() {
        return cachedMsgMinOffset;
    }

    public void setCachedMsgMinOffset(long cachedMsgMinOffset) {
        this.cachedMsgMinOffset = cachedMsgMinOffset;
    }

    public long getCachedMsgMaxOffset() {
        return cachedMsgMaxOffset;
    }

    public void setCachedMsgMaxOffset(long cachedMsgMaxOffset) {
        this.cachedMsgMaxOffset = cachedMsgMaxOffset;
    }

    public int getCachedMsgCount() {
        return cachedMsgCount;
    }

    public void setCachedMsgCount(int cachedMsgCount) {
        this.cachedMsgCount = cachedMsgCount;
    }

    public long getTransactionMsgMinOffset() {
        return transactionMsgMinOffset;
    }

    public void setTransactionMsgMinOffset(long transactionMsgMinOffset) {
        this.transactionMsgMinOffset = transactionMsgMinOffset;
    }

    public long getTransactionMsgMaxOffset() {
        return transactionMsgMaxOffset;
    }

    public void setTransactionMsgMaxOffset(long transactionMsgMaxOffset) {
        this.transactionMsgMaxOffset = transactionMsgMaxOffset;
    }

    public int getTransactionMsgCount() {
        return transactionMsgCount;
    }

    public void setTransactionMsgCount(int transactionMsgCount) {
        this.transactionMsgCount = transactionMsgCount;
    }

    /** 返回是否已加锁。 */
    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getTryUnlockTimes() {
        return tryUnlockTimes;
    }

    public void setTryUnlockTimes(long tryUnlockTimes) {
        this.tryUnlockTimes = tryUnlockTimes;
    }

    public long getLastLockTimestamp() {
        return lastLockTimestamp;
    }

    public void setLastLockTimestamp(long lastLockTimestamp) {
        this.lastLockTimestamp = lastLockTimestamp;
    }

    /** 返回队列是否已丢弃。 */
    public boolean isDroped() {
        return droped;
    }

    public void setDroped(boolean droped) {
        this.droped = droped;
    }

    public long getLastPullTimestamp() {
        return lastPullTimestamp;
    }

    public void setLastPullTimestamp(long lastPullTimestamp) {
        this.lastPullTimestamp = lastPullTimestamp;
    }

    public long getLastConsumeTimestamp() {
        return lastConsumeTimestamp;
    }

    public void setLastConsumeTimestamp(long lastConsumeTimestamp) {
        this.lastConsumeTimestamp = lastConsumeTimestamp;
    }

    public int getCachedMsgSizeInMiB() {
        return cachedMsgSizeInMiB;
    }

    public void setCachedMsgSizeInMiB(final int cachedMsgSizeInMiB) {
        this.cachedMsgSizeInMiB = cachedMsgSizeInMiB;
    }

    /** 返回便于诊断的可读字符串。 */
    @Override
    public String toString() {
        return "ProcessQueueInfo [commitOffset=" + commitOffset + ", cachedMsgMinOffset="
            + cachedMsgMinOffset + ", cachedMsgMaxOffset=" + cachedMsgMaxOffset
            + ", cachedMsgCount=" + cachedMsgCount + ", cachedMsgSizeInMiB=" + cachedMsgSizeInMiB
            + ", transactionMsgMinOffset=" + transactionMsgMinOffset
            + ", transactionMsgMaxOffset=" + transactionMsgMaxOffset + ", transactionMsgCount="
            + transactionMsgCount + ", locked=" + locked + ", tryUnlockTimes=" + tryUnlockTimes
            + ", lastLockTimestamp=" + UtilAll.timeMillisToHumanString(lastLockTimestamp) + ", droped="
            + droped + ", lastPullTimestamp=" + UtilAll.timeMillisToHumanString(lastPullTimestamp)
            + ", lastConsumeTimestamp=" + UtilAll.timeMillisToHumanString(lastConsumeTimestamp) + "]";
    }
}
