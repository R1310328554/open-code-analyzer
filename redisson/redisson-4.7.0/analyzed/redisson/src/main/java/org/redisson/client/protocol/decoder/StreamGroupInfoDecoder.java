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

import org.redisson.api.stream.StreamGroup;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.convertor.StreamIdConvertor;

import java.util.List;
import java.util.Optional;

/**
 * Stream 消费组信息解码器。
 * <p>
 * 解析 {@code XINFO GROUPS} 返回的键值交替序列，
 * 提取组名、消费者数、待处理数、最后投递 ID 及可选的 lag 统计。
 *
 * @author Nikita Koksharov
 *
 */
public class StreamGroupInfoDecoder implements MultiDecoder<StreamGroup> {

    /** 根据字段数量区分新旧响应格式并构造 {@link StreamGroup}。 */
    @Override
    public StreamGroup decode(List<Object> parts, State state) {
        // 旧版响应仅含 4 组键值对（8 个元素）
        if (parts.size() == 8) {
            return new StreamGroup((String) parts.get(1),
                                    ((Long) parts.get(3)).intValue(),
                                    ((Long) parts.get(5)).intValue(),
                                    StreamIdConvertor.INSTANCE.convert(parts.get(7)));
        }

        return new StreamGroup((String) parts.get(1),
                ((Long) parts.get(3)).intValue(),
                ((Long) parts.get(5)).intValue(),
                StreamIdConvertor.INSTANCE.convert(parts.get(7)),
                Optional.ofNullable((Long) parts.get(9)).orElse(0L).intValue(),
                Optional.ofNullable((Long) parts.get(11)).orElse(0L).intValue());
    }

}
