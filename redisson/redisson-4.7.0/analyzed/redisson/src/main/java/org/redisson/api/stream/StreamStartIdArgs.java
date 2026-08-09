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
 * 流范围查询的起始消息 ID 参数接口。
 * <p>
 * 支持包含或排除起始 ID 两种边界模式。
 *
 * @author seakider
 *
 */
public interface StreamStartIdArgs<T> {
    /**
     * 设置范围起始 ID（包含边界）。
     *
     * @param startId 起始消息 ID
     * @return 后续选项
     */
    StreamEndIdArgs<T> startId(StreamMessageId startId);

    /**
     * 设置范围起始 ID（不包含边界）。
     *
     * @param startId 起始消息 ID
     * @return 后续选项
     */
    StreamEndIdArgs<T> startIdExclusive(StreamMessageId startId);
}
