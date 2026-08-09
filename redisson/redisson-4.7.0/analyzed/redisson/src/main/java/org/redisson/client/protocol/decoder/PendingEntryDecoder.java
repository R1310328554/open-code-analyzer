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

import org.redisson.api.stream.PendingEntry;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.convertor.StreamIdConvertor;

import java.util.List;

/**
 * Redis Stream 待处理消息（XPENDING 明细）解码器。
 * <p>
 * 将 {@code [id, consumer, idle_ms, delivery_count]} 四元组转为
 * {@link PendingEntry}；若 parts 已含 {@link PendingEntry} 实例或为空则原样返回。
 *
 * @author Nikita Koksharov
 *
 */
public class PendingEntryDecoder implements MultiDecoder<Object> {

    /** 流消息 ID 字符串与 {@link StreamMessageId} 之间的转换器。 */
    private final StreamIdConvertor convertor = new StreamIdConvertor();

    /** 解析单条待处理记录，或透传已解码的 PendingEntry 列表。 */
    @Override
    public Object decode(List<Object> parts, State state) {
        if (parts.isEmpty() || parts.get(0) instanceof PendingEntry) {
            return parts;
        }
        return new PendingEntry(convertor.convert(parts.get(0)), parts.get(1).toString(), 
                Long.parseLong(parts.get(2).toString()), Long.parseLong(parts.get(3).toString()));
    }

}
