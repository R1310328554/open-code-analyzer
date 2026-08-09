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
 * 定义范围查询结束 ID 的参数接口。
 *
 * @author seakider
 *
 */
public interface StreamEndIdArgs<T> {
    /**
     * 设置范围结束 ID（含边界）。
     *
     * @param endId 结束消息 ID
     * @return 后续选项
     */
    T endId(StreamMessageId endId);

    /**
     * 设置范围结束 ID（不含边界）。
     *
     * @param endId 结束消息 ID
     * @return 后续选项
     */
    T endIdExclusive(StreamMessageId endId);
}

