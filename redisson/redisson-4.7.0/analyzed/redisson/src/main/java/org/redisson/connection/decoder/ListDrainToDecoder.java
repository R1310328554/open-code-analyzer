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
package org.redisson.connection.decoder;

import org.redisson.client.handler.State;
import org.redisson.client.protocol.decoder.MultiDecoder;

import java.util.Collection;
import java.util.List;

/**
 * 将 Redis 批量响应元素追加到外部集合并返回元素个数的解码器。
 * <p>
 * 常用于管道化命令，由调用方提供可变集合接收结果。
 */
public class ListDrainToDecoder<V> implements MultiDecoder<Integer> {

    /** 接收解码元素的 target 集合（通常为 ArrayList）。 */
    private Collection<Object> list;

    /** @param list 用于接收响应元素的集合 */
    public ListDrainToDecoder(Collection<Object> list) {
        this.list = list;
    }

    /** 将全部 parts 追加到 list 并返回本次追加的元素数量。 */
    @Override
    public Integer decode(List<Object> parts, State state) {
        list.addAll(parts);
        return parts.size();
    }

}
