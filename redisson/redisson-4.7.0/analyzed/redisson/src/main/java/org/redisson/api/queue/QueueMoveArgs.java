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
package org.redisson.api.queue;

import org.redisson.api.SyncArgs;

/**
 * 定义队列间转移消息的参数接口。
 *
 * @author Nikita Koksharov
 *
 */
public interface QueueMoveArgs extends SyncArgs<QueueMoveArgs> {

    /**
     * 按消息 ID 指定要转移到目标队列的消息。
     *
     * @param ids 待转移消息的标识符
     * @return 参数对象
     */
    static QueueMoveDestination ids(String... ids) {
        return new QueueMoveParams(ids);
    }

    /**
     * 指定从本队列队首转移到目标队列队尾的消息条数。
     *
     * @param count 从队首转移的元素数量
     * @return 参数对象
     */
    static QueueMoveDestination count(int count) {
        return new QueueMoveParams(count);
    }

}
