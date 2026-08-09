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
package org.redisson.spring.data.connection;

import java.util.Properties;

import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * 将 Redis INFO/CONFIG 类 colon 分隔文本解码为 {@link Properties}。
 * <p>按行拆分 {@code key:value}，忽略格式非法行。
 *
 * @author Nikita Koksharov
 *
 */
public class PropertiesDecoder implements Decoder<Properties> {

    /** 从 Netty {@link ByteBuf} 读取 UTF-8 文本并解析为属性表。 */
    @Override
    public Properties decode(ByteBuf buf, State state) {
        String value = buf.toString(CharsetUtil.UTF_8);
        Properties result = new Properties();
        for (String entry : value.split("\n")) {
            if (entry.length() < 2) {
                continue;
            }
            String[] pair = entry.split(":");
            if (pair.length != 2 || pair[0].length() == 0 ) {
                continue;
            }
            String second = pair[1];
            // 去除 Windows 换行残留 \r。
            if (second.charAt(second.length() - 1) == '\r') {
                second = second.substring(0, second.length() - 1);
            }
            result.put(pair[0], second);
        }
        return result;
    }

}
