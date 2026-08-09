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
import java.util.concurrent.TimeUnit;

/**
 * {@code XAUTOCLAIM} 快速模式的返回结果对象。
 * <p>
 * 仅返回消息 ID 列表，不携带消息体，适用于只需转移所有权而不读取内容的场景。
 *
 * @see RStream#fastAutoClaim(String, String, long, TimeUnit, StreamMessageId, int)
 * @see RStreamAsync#fastAutoClaimAsync(String, String, long, TimeUnit, StreamMessageId, int)
 * 
 * @author Nikita Koksharov
 *
 */
public class FastAutoClaimResult implements Serializable {

    private static final long serialVersionUID = -5525031552305408248L;

    /** 下次 autoClaim 扫描的起始消息 ID。 */
    private StreamMessageId nextId;
    /** 已认领的消息 ID 列表。 */
    private List<StreamMessageId> ids;

    public FastAutoClaimResult() {
    }

    public FastAutoClaimResult(StreamMessageId nextId, List<StreamMessageId> ids) {
        super();
        this.nextId = nextId;
        this.ids = ids;
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
     * 返回已认领的消息 ID 列表。
     *
     * @return 消息 ID 列表
     */
    public List<StreamMessageId> getIds() {
        return ids;
    }
}
