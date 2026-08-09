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
package org.redisson.api.pubsub.event;

import org.redisson.api.pubsub.PubSubOperation;

/**
 * PubSub 操作被禁用事件的监听器接口。
 * <p>
 * 当指定主题的某项 PubSub 操作切换为禁用状态时触发。
 *
 * @author Nikita Koksharov
 *
 */
public interface DisabledOperationEventListener extends PubSubEventListener {

    /**
     * PubSub 操作切换为禁用状态时调用。
     *
     * @param topicName 主题名称
     * @param operation 被禁用的操作
     */
    void onDisabled(String topicName, PubSubOperation operation);

}
