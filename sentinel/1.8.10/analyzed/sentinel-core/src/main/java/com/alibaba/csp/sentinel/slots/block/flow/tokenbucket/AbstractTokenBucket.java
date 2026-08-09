/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.slots.block.flow.tokenbucket;

import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.TimeUtil;

/**
 * 令牌桶算法的抽象基类，负责按时间间隔补充令牌并支持消费。
 *
 * @author LearningGp
 */
public class AbstractTokenBucket implements TokenBucket{
    protected final long MAX_UNIT_PRODUCE_NUM = Long.MAX_VALUE;

    /**
     * 桶中剩余令牌数
     */
    protected volatile long currentTokenNum;

    /**
     * 下次生产令牌的时间戳
     */
    protected volatile long nextProduceTime;

    /**
     * 每个时间单位生产的令牌数
     */
    protected final long unitProduceNum;

    /**
     * 桶中可存储的最大令牌数
     */
    protected final long maxTokenNum;

    protected final long intervalInMs;
    protected final long startTime;

    public AbstractTokenBucket(long unitProduceNum, long maxTokenNum, boolean fullStart, long intervalInMs) {
        AssertUtil.isTrue(unitProduceNum > 0 && intervalInMs > 0 && unitProduceNum < MAX_UNIT_PRODUCE_NUM,
                "Illegal unitProduceNum or intervalInSeconds");
        AssertUtil.isTrue(maxTokenNum > 0, "Illegal maxTokenNum");
        this.unitProduceNum = unitProduceNum;
        this.maxTokenNum = maxTokenNum;
        this.intervalInMs = intervalInMs;
        this.startTime = TimeUtil.currentTimeMillis();
        this.nextProduceTime = startTime;
        if (fullStart) {
            this.currentTokenNum = maxTokenNum;
        } else {
            //The token will be filled when the first request arrives (including the initial token)
            this.currentTokenNum = 0;
        }
    }

    @Override
    public boolean tryConsume(long tokenNum) {
        if (tokenNum <= 0) {
            return true;
        }
        if (tokenNum > maxTokenNum) {
            return false;
        }
        long currentTimestamp = TimeUtil.currentTimeMillis();
        refreshCurrentTokenNum(currentTimestamp);
        if (tokenNum <= currentTokenNum) {
            currentTokenNum -= tokenNum;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void refreshCurrentTokenNum(long currentTimestamp) {
        if (nextProduceTime > currentTimestamp) {
            return;
        }
        currentTokenNum = Math.min(maxTokenNum, currentTokenNum + calProducedTokenNum(currentTimestamp));
        updateNextProduceTime(currentTimestamp);
    }

    protected long calProducedTokenNum(long currentTimestamp) {
        if (nextProduceTime > currentTimestamp) {
            return 0;
        }
        long nextRefreshUnitCount = (nextProduceTime - startTime) / intervalInMs;
        long currentUnitCount = (currentTimestamp - startTime) / intervalInMs;
        long unitCount = currentUnitCount - nextRefreshUnitCount + 1;
        return unitCount * unitProduceNum;
    }

    protected void updateNextProduceTime(long currentTimestamp) {
        nextProduceTime = intervalInMs - ((currentTimestamp - startTime) % intervalInMs) + currentTimestamp;
    }

    public long refreshTokenAndGetCurrentTokenNum() {
        refreshCurrentTokenNum(TimeUtil.currentTimeMillis());
        return currentTokenNum;
    }

    public long getCurrentTokenNum() {
        return currentTokenNum;
    }

}
