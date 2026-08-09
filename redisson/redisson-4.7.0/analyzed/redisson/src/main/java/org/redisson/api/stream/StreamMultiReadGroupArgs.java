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
 * {@code RStream.readGroup()} 多流读取方法的参数对象。
 *
 * @author Nikita Koksharov
 *
 */
public interface StreamMultiReadGroupArgs {

    /**
     * 认领空闲时间至少达到指定最小时长的待处理消息。
     *
     * @param duration 最小空闲时长
     * @return 参数对象
     */
    StreamMultiReadGroupArgs claim(Duration duration);

    /**
     * 设置读取后不将消息加入待处理条目列表（PEL）。
     *
     * @return 参数对象
     */
    StreamMultiReadGroupArgs noAck();

    /**
     * 设置单个流返回的数据条数上限。
     *
     * @param count 单流数据条数上限
     * @return 参数对象
     */
    StreamMultiReadGroupArgs count(int count);

    /**
     * 设置命令返回的条目总数上限。
     * <p>
     * 与 {@link #count(int)} 按流分别限制不同，
     * 此限制作用于所有流返回条目的累计数量。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上版本。</b>
     *
     * @param maxCount 条目总数上限
     * @return 参数对象
     */
    StreamMultiReadGroupArgs maxCount(int maxCount);

    /**
     * 设置命令返回条目的总字节数上限。
     * <p>
     * 限制作用于所有流返回条目的累计字节大小。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上版本。</b>
     *
     * @param maxSize 条目总字节数上限
     * @return 参数对象
     */
    StreamMultiReadGroupArgs maxSize(long maxSize);

    /**
     * 设置等待流数据可用的时间间隔。
     * 使用 <code>0</code> 表示无限等待。
     *
     * @param timeout 超时时间
     * @return 参数对象
     */
    StreamMultiReadGroupArgs timeout(Duration timeout);

    /**
     * 从所有指定流中读取消息 ID 大于给定起始 ID 的消息。
     *
     * @param id1 当前流的上次读取消息 ID
     * @param stream2 第二个流的名称
     * @param id2 第二个流的上次读取消息 ID
     * @return 参数对象
     */
    static StreamMultiReadGroupArgs greaterThan(StreamMessageId id1,
                                           String stream2, StreamMessageId id2) {
        return greaterThan(id1, Collections.singletonMap(stream2, id2));
    }

    /**
     * 从所有指定流中读取消息 ID 大于给定起始 ID 的消息。
     *
     * @param id1 当前流的上次读取消息 ID
     * @param stream2 第二个流的名称
     * @param id2 第二个流的上次读取消息 ID
     * @param stream3 第三个流的名称
     * @param id3 第三个流的上次读取消息 ID
     * @return 参数对象
     */
    static StreamMultiReadGroupArgs greaterThan(StreamMessageId id1,
                                            String stream2, StreamMessageId id2,
                                            String stream3, StreamMessageId id3) {
        Map<String, StreamMessageId> map = new HashMap<>();
        map.put(stream2, id2);
        map.put(stream3, id3);
        return greaterThan(id1, map);
    }

    /**
     * 从所有指定流中读取消息 ID 大于给定起始 ID 的消息。
     *
     * @param id1 当前流的上次读取消息 ID
     * @param stream2 第二个流的名称
     * @param id2 第二个流的上次读取消息 ID
     * @param stream3 第三个流的名称
     * @param id3 第三个流的上次读取消息 ID
     * @param stream4 第四个流的名称
     * @param id4 第四个流的上次读取消息 ID
     * @return 参数对象
     */
    static StreamMultiReadGroupArgs greaterThan(StreamMessageId id1,
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
     * 从所有指定流中读取消息 ID 大于给定起始 ID 的消息。
     *
     * @param id1 当前流的上次读取消息 ID
     * @param stream2 第二个流的名称
     * @param id2 第二个流的上次读取消息 ID
     * @param stream3 第三个流的名称
     * @param id3 第三个流的上次读取消息 ID
     * @param stream4 第四个流的名称
     * @param id4 第四个流的上次读取消息 ID
     * @param stream5 第五个流的名称
     * @param id5 第五个流的上次读取消息 ID
     * @return 参数对象
     */
    static StreamMultiReadGroupArgs greaterThan(StreamMessageId id1,
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
     * 从所有指定流中读取消息 ID 大于给定起始 ID 的消息。
     *
     * @param id 当前流的上次读取消息 ID
     * @param offsets 各流名称到上次读取消息 ID 的映射
     * @return 参数对象
     */
    static StreamMultiReadGroupArgs greaterThan(StreamMessageId id, Map<String, StreamMessageId> offsets) {
        return new StreamMultiReadGroupParams(id, offsets);
    }

}
