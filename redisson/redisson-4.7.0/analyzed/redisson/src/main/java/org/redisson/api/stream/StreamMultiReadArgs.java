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

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link org.redisson.api.RStream#read} 系列方法的参数对象。
 * <p>
 * 支持多流联合读取、超时等待及返回条数限制等配置。
 *
 * @author Nikita Koksharov
 *
 */
public interface StreamMultiReadArgs {

    /**
     * 设置每个流返回的数据条数上限。
     *
     * @param count 条数上限
     * @return 参数对象
     */
    StreamMultiReadArgs count(int count);

    /**
     * 设置命令返回的条目总数上限。
     * <p>
     * 与 {@link #count(int)} 按单流限制不同，此限制作用于所有流的累计条目数。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上版本。</b>
     *
     * @param maxCount 条目总数上限
     * @return 参数对象
     */
    StreamMultiReadArgs maxCount(int maxCount);

    /**
     * 设置命令返回条目的总字节数上限。
     * <p>
     * 限制作用于所有流返回条目的累计大小。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上版本。</b>
     *
     * @param maxSize 总字节数上限
     * @return 参数对象
     */
    StreamMultiReadArgs maxSize(long maxSize);

    /**
     * 设置等待流数据可用的超时时间。
     * <p>
     * 传入 <code>0</code> 表示无限等待。
     *
     * @param timeout 超时时长
     * @return 参数对象
     */
    StreamMultiReadArgs timeout(Duration timeout);

    /**
     * 定义各流（含当前流）上次读取到的消息 ID。
     * <p>
     * 从所有指定流中读取 ID 大于给定值的条目。
     *
     * @param id1 当前流的上次 ID
     * @param stream2 第 2 个流名称
     * @param id2 第 2 个流的上次 ID
     * @return 参数对象
     */
    static StreamMultiReadArgs greaterThan(StreamMessageId id1,
                                           String stream2, StreamMessageId id2) {
        return greaterThan(id1, Collections.singletonMap(stream2, id2));
    }

    /**
     * 定义各流（含当前流）上次读取到的消息 ID（三流版本）。
     * <p>
     * 从所有指定流中读取 ID 大于给定值的条目。
     *
     * @param id1 当前流的上次 ID
     * @param stream2 第 2 个流名称
     * @param id2 第 2 个流的上次 ID
     * @param stream3 第 3 个流名称
     * @param id3 第 3 个流的上次 ID
     * @return 参数对象
     */
    static StreamMultiReadArgs greaterThan(StreamMessageId id1,
                                           String stream2, StreamMessageId id2,
                                           String stream3, StreamMessageId id3) {
        Map<String, StreamMessageId> map = new HashMap<>();
        map.put(stream2, id2);
        map.put(stream3, id3);
        return greaterThan(id1, map);
    }

    /**
     * 定义各流（含当前流）上次读取到的消息 ID（四流版本）。
     * <p>
     * 从所有指定流中读取 ID 大于给定值的条目。
     *
     * @param id1 当前流的上次 ID
     * @param stream2 第 2 个流名称
     * @param id2 第 2 个流的上次 ID
     * @param stream3 第 3 个流名称
     * @param id3 第 3 个流的上次 ID
     * @param stream4 第 4 个流名称
     * @param id4 第 4 个流的上次 ID
     * @return 参数对象
     */
    static StreamMultiReadArgs greaterThan(StreamMessageId id1,
                                           String stream2, StreamMessageId id2,
                                           String stream3, StreamMessageId id3,
                                           String stream4, StreamMessageId id4) {
        Map<String, StreamMessageId> map = new HashMap<>();
        map.put(stream2, id2);
        map.put(stream3, id3);
        map.put(stream4, id4);
        return greaterThan(id1, map);
    }

    /**
     * 定义各流（含当前流）上次读取到的消息 ID（五流版本）。
     * <p>
     * 从所有指定流中读取 ID 大于给定值的条目。
     *
     * @param id1 当前流的上次 ID
     * @param stream2 第 2 个流名称
     * @param id2 第 2 个流的上次 ID
     * @param stream3 第 3 个流名称
     * @param id3 第 3 个流的上次 ID
     * @param stream4 第 4 个流名称
     * @param id4 第 4 个流的上次 ID
     * @param stream5 第 5 个流名称
     * @param id5 第 5 个流的上次 ID
     * @return 参数对象
     */
    static StreamMultiReadArgs greaterThan(StreamMessageId id1,
                                           String stream2, StreamMessageId id2,
                                           String stream3, StreamMessageId id3,
                                           String stream4, StreamMessageId id4,
                                           String stream5, StreamMessageId id5) {
        Map<String, StreamMessageId> map = new HashMap<>();
        map.put(stream2, id2);
        map.put(stream3, id3);
        map.put(stream4, id4);
        map.put(stream5, id5);
        return greaterThan(id1, map);
    }

    /**
     * 通过映射定义各流上次读取到的消息 ID。
     * <p>
     * 从所有指定流中读取 ID 大于给定值的条目。
     *
     * @param id1 当前流的上次 ID
     * @param offsets 流名称到上次 ID 的映射
     * @return 参数对象
     */
    static StreamMultiReadArgs greaterThan(StreamMessageId id1, Map<String, StreamMessageId> offsets) {
        return new StreamMultiReadParams(id1, offsets);
    }

}
