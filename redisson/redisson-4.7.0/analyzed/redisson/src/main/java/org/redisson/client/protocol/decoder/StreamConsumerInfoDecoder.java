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

import org.redisson.api.stream.StreamConsumer;
import org.redisson.client.handler.State;

import java.util.List;

/**
 * Stream 消费者信息解码器。
 * <p>
 * 解析 {@code XINFO CONSUMERS} 返回的键值交替序列，
 * 提取消费者名称、待处理数、空闲时间与可选的活跃时间。
 *
 * @author Nikita Koksharov
 *
 */
public class StreamConsumerInfoDecoder implements MultiDecoder<StreamConsumer> {

    /** 从键值对序列构造 {@link StreamConsumer} 实例。 */
    @Override
    public StreamConsumer decode(List<Object> parts, State state) {
        // 含 active-time 字段时 parts 长度大于 6
        if (parts.size() > 6) {
            return new StreamConsumer((String) parts.get(1),
                    ((Long) parts.get(3)).intValue(), (Long) parts.get(5), (Long) parts.get(7));
        }
        return new StreamConsumer((String) parts.get(1),
                ((Long) parts.get(3)).intValue(), (Long) parts.get(5), -1);
    }

}
