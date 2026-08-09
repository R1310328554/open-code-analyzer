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

/**
 * {@code RStream.range()} 流范围查询方法的参数对象。
 *
 * @author seakider
 */
public interface StreamRangeArgs {

    /**
     * 设置范围查询返回的条目数量上限。
     *
     * @param count 范围条目数量上限
     * @return 参数对象
     */
    StreamRangeArgs count(int count);

    /**
     * 设置范围起始消息 ID（包含边界）。
     *
     * @param startId 起始消息 ID
     * @return 下一步参数选项
     */
    static StreamEndIdArgs<StreamRangeArgs> startId(StreamMessageId startId) {
        return new StreamRangeParams(startId, false);
    }

    /**
     * 设置范围起始消息 ID（不包含边界）。
     *
     * @param startId 起始消息 ID
     * @return 下一步参数选项
     */
    static StreamEndIdArgs<StreamRangeArgs> startIdExclusive(StreamMessageId startId) {
        return new StreamRangeParams(startId, true);
    }
}
