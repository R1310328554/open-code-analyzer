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

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * 消息过滤接口：支持基于 ConsumeQueue 扩展单元或 CommitLog 内容的匹配。
 */
public interface MessageFilter {
    /**
     * 基于 Tag 哈希码或 ConsumeQueue 扩展单元中的布隆位图匹配。
     *
     * @param tagsCode Tag 哈希码
     * @param cqExtUnit ConsumeQueue 扩展单元
     */
    boolean isMatchedByConsumeQueue(final Long tagsCode,
        final ConsumeQueueExt.CqExtUnit cqExtUnit);

    /**
     * 基于 CommitLog 中消息体内容匹配（如 SQL92 表达式）。
     * <br>{@code msgBuffer} 与 {@code properties} 不会同时非 null：Store 内调用时 properties 为 null；
     * {@code PullRequestHoldService} 内调用时 msgBuffer 为 null。
     *
     * @param msgBuffer CommitLog 消息缓冲区，Store 外可能为 null
     * @param properties 消息属性，为 null 时需自行从 buffer 解码
     */
    boolean isMatchedByCommitLog(final ByteBuffer msgBuffer,
        final Map<String, String> properties);
}
