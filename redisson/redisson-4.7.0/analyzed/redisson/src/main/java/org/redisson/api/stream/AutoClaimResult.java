/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.stream;

import org.redisson.api.RStream;
import org.redisson.api.RStreamAsync;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * {@code XAUTOCLAIM} 命令的完整返回结果对象。
 * <p>
 * 包含下次扫描起始 ID、已认领消息及其内容，以及因引用策略被删除的消息 ID 列表。
 *
 * @see RStream#autoClaim(String, String, long, TimeUnit, StreamMessageId, int)
 * @see RStreamAsync#autoClaimAsync(String, String, long, TimeUnit, StreamMessageId, int)
 * 
 * @author Nikita Koksharov
 *
 */
public final class AutoClaimResult<K, V> implements Serializable {

    private static final long serialVersionUID = -5525031552305408248L;

    /** 下次 autoClaim 扫描的起始消息 ID。 */
    private StreamMessageId nextId;
    /** 已认领的消息，键为消息 ID，值为字段映射。 */
    private Map<StreamMessageId, Map<K, V>> messages;
    /** 因引用策略被删除的消息 ID 列表。 */
    private List<StreamMessageId> deletedIds;

    public AutoClaimResult() {
    }

    public AutoClaimResult(StreamMessageId nextId, Map<StreamMessageId, Map<K, V>> messages, List<StreamMessageId> deletedIds) {
        super();
        this.nextId = nextId;
        this.messages = messages;
        this.deletedIds = deletedIds;
    }

    /**
     * 返回下次扫描的起始消息 ID。
     *
     * @return 起始 ID
     */
    public StreamMessageId getNextId() {
        return nextId;
    }

    /**
     * 返回已认领的消息及其字段内容。
     *
     * @return 消息 ID 到字段映射的映射
     */
    public Map<StreamMessageId, Map<K, V>> getMessages() {
        return messages;
    }

    /**
     * 返回被删除的消息 ID 列表。
     *
     * @return 已删除消息 ID 列表
     */
    public List<StreamMessageId> getDeletedIds() {
        return deletedIds;
    }
}
