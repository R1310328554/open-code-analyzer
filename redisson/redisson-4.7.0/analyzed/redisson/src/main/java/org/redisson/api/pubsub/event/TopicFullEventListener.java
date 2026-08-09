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

/**
 * PubSub 主题已满事件的监听器接口。
 * <p>
 * 当主题缓冲区达到容量上限、无法接受更多消息时触发。
 *
 * @author Nikita Koksharov
 *
 */
public interface TopicFullEventListener extends PubSubEventListener {

    /**
     * PubSub 主题已满时调用。
     *
     * @param topicName 主题名称
     */
    void onFull(String topicName);

}
