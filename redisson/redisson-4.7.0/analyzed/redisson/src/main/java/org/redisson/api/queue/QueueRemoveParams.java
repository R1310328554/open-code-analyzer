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

import org.redisson.api.BaseSyncParams;

/**
 * {@link QueueRemoveArgs} 的可变实现，保存待从队列中移除的消息 ID 列表。
 *
 * @author Nikita Koksharov
 *
 */
public class QueueRemoveParams extends BaseSyncParams<QueueRemoveArgs> implements QueueRemoveArgs {

    private final String[] ids;

    /** 构造移除参数，绑定待移除的消息 ID 数组。 */
    public QueueRemoveParams(String[] ids) {
        this.ids = ids;
    }

    /** 返回待移除的消息 ID 列表。 */
    public String[] getIds() {
        return ids;
    }
}
