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
 * 定义待操作消息 ID 列表的参数接口。
 *
 * @author seakider
 *
 */
public interface StreamMessageIdArgs<T> {

    /**
     * 指定一条或多条消息 ID。
     *
     * @param ids 消息 ID 数组
     * @return 参数对象
     */
    T ids(StreamMessageId... ids);
}
