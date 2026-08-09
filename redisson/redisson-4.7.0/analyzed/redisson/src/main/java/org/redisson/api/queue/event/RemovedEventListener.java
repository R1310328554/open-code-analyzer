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
package org.redisson.api.queue.event;

import java.util.List;

/**
 * 队列消息移除事件监听器。
 * 当消息从队列中被移除时触发。
 *
 * @author Nikita Koksharov
 *
 */
public interface RemovedEventListener extends QueueEventListener {

    /**
     * 消息从队列被移除时回调。
     *
     * @param ids 被移除的消息 ID 列表
     */
    void onRemoved(List<String> ids);

}
