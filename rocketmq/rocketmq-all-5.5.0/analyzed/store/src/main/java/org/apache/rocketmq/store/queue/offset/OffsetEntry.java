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

package org.apache.rocketmq.store.queue.offset;

/**
 * 消费队列与 CommitLog 偏移边界条目（最大/最小）。
 */
public class OffsetEntry {
    /**
     * Topic 标识；当前直接使用 Topic 名称，后续可改为定长标识。
     */
    /** Topic 标识。 */
    public String topic;

    /**
     * 队列 ID。
     */
    /** 队列 ID。 */
    public int queueId;

    /**
     * 标记该条目表示最大值还是最小值。
     */
    /** 最大/最小类型。 */
    public OffsetEntryType type;

    /**
     * 消费队列的最大或最小偏移量。
     */
    /** 消费队列偏移。 */
    public long offset;

    /**
     * CommitLog 的最大或最小物理偏移。
     */
    /** CommitLog 物理偏移。 */
    public long commitLogOffset;
}
