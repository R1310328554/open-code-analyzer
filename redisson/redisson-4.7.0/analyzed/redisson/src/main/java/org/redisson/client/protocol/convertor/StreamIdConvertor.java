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
package org.redisson.client.protocol.convertor;

import org.redisson.api.stream.StreamMessageId;

/**
 * Redis Stream 消息 ID 字符串到 {@link StreamMessageId} 的转换器。
 * <p>
 * 输入格式为 {@code 毫秒-序号}（如 {@code 1609459200000-0}），按 {@code -} 拆分后解析。
 *
 * @author Nikita Koksharov
 *
 */
public class StreamIdConvertor implements Convertor<StreamMessageId> {

    /** 单例实例，供命令定义复用。 */
    public static final StreamIdConvertor INSTANCE = new StreamIdConvertor();
    
    /** 解析 {@code ms-seq} 形式 ID 为 {@link StreamMessageId}。 */
    @Override
    public StreamMessageId convert(Object id) {
        String[] parts = id.toString().split("-");
        return new StreamMessageId(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }

}
