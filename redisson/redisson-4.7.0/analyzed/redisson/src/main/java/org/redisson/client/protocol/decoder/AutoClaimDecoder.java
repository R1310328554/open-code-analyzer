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
package org.redisson.client.protocol.decoder;

import org.redisson.api.stream.AutoClaimResult;
import org.redisson.api.stream.StreamMessageId;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@code XAUTOCLAIM} 命令完整回复解码器。
 * <p>
 * 解析起始消息 ID、已认领消息映射及可选的已删除 ID 列表，
 * 组装为 {@link AutoClaimResult}。
 *
 * @author Nikita Koksharov
 *
 */
public class AutoClaimDecoder implements MultiDecoder<Object> {

    /** 嵌套字段使用 {@link StreamIdDecoder} 解析 Stream 消息 ID。 */
    @Override
    public Decoder<Object> getDecoder(Codec codec, int paramNum, State state, long size) {
        return new StreamIdDecoder();
    }

    /** 空回复返回 {@code null}；否则按 [nextId, messages, deletedIds?] 构造结果。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        // 无待认领消息时 Redis 返回空数组
        if (parts.isEmpty()) {
            return null;            
        }

        Map<StreamMessageId, Map<Object, Object>> maps = (Map<StreamMessageId, Map<Object, Object>>) parts.get(1);
        // 第三段为可选的已删除消息 ID 列表
        List<StreamMessageId> deletedIds = Collections.emptyList();
        if (parts.size() == 3) {
            deletedIds = (List<StreamMessageId>) parts.get(2);
        }
        return new AutoClaimResult((StreamMessageId) parts.get(0), maps, deletedIds);
    }

}
