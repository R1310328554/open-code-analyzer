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

/**
 * {@code RStream.read()} 单流读取方法的参数对象。
 *
 * @author Nikita Koksharov
 *
 */
public interface StreamReadArgs {

    /**
     * 设置返回的数据条数上限。
     *
     * @param count 数据条数上限
     * @return 参数对象
     */
    StreamReadArgs count(int count);

    /**
     * 设置命令回复中返回的条目总数上限。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上版本。</b>
     *
     * @param maxCount 条目总数上限
     * @return 参数对象
     */
    StreamReadArgs maxCount(int maxCount);

    /**
     * 设置命令回复中返回条目的总字节数上限。
     * <p>
     * 需要 <b>Redis 8.10.0 及以上版本。</b>
     *
     * @param maxSize 条目总字节数上限
     * @return 参数对象
     */
    StreamReadArgs maxSize(long maxSize);

    /**
     * 设置等待流数据可用的时间间隔。
     * 使用 <code>0</code> 表示无限等待。
     *
     * @param timeout 超时时间
     * @return 参数对象
     */
    StreamReadArgs timeout(Duration timeout);

    /**
     * 读取当前流中消息 ID 大于指定 ID 的数据。
     *
     * @param id0 当前流的上次读取消息 ID
     * @return 参数对象
     */
    static StreamReadArgs greaterThan(StreamMessageId id0) {
        return new StreamReadParams(id0);
    }

}
