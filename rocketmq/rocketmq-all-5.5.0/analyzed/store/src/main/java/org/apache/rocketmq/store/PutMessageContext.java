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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.rocketmq.store;

/**
 * 批量写消息上下文：关联 Topic-Queue 键、物理位置数组与批次大小。
 */
public class PutMessageContext {
    /** Topic 与 Queue 组合键。 */
    private String topicQueueTableKey;
    /** 各条消息在 CommitLog 中的物理偏移数组。 */
    private long[] phyPos;
    /** 本批次消息条数。 */
    private int batchSize;

    /** 指定 Topic-Queue 键构造上下文。 */
    public PutMessageContext(String topicQueueTableKey) {
        this.topicQueueTableKey = topicQueueTableKey;
    }

    /** 返回 Topic-Queue 键。 */
    public String getTopicQueueTableKey() {
        return topicQueueTableKey;
    }

    /** 返回物理偏移数组。 */
    public long[] getPhyPos() {
        return phyPos;
    }

    /** 设置物理偏移数组。 */
    public void setPhyPos(long[] phyPos) {
        this.phyPos = phyPos;
    }

    /** 返回批次大小。 */
    public int getBatchSize() {
        return batchSize;
    }

    /** 设置批次大小。 */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}