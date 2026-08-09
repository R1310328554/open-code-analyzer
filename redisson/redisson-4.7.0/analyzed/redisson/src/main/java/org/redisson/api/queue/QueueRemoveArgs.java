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
 * 定义队列消息移除操作的参数。
 *
 * @author Nikita Koksharov
 *
 */
public interface QueueRemoveArgs extends SyncArgs<QueueRemoveArgs> {

    /**
     * 按消息 ID 指定要从队列中移除的消息。
     *
     * @param ids 待移除的消息 ID
     * @return 参数对象
     */
    static QueueRemoveArgs ids(String... ids) {
        return new QueueRemoveParams(ids);
    }

}
