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
package org.redisson.api.pubsub;

/**
 * 定义主题中消息的保留策略，控制消息何时存储以及何时根据订阅与处理状态丢弃。
 * <p>
 * 保留模式决定：
 * <ul>
 *   <li>存储消息是否要求存在活跃订阅者</li>
 *   <li>已处理完成的消息是保留还是删除</li>
 * </ul>
 *
 * @author Nikita Koksharov
 *
 */
public enum RetentionMode {

    /**
     * 要求至少有一个订阅者才在主题中存储消息。当所有订阅均已确认、
     * 达到重投上限或否定确认失败后，消息将被丢弃。
     * <p>
     * 仅考虑消息发布时已存在的订阅。
     */
    SUBSCRIPTION_REQUIRED_DELETE_PROCESSED,

    /**
     * 要求至少有一个订阅者才存储消息。所有订阅处理完毕（确认、
     * 达到重投上限或否定确认）后消息仍保留在主题中。
     */
    SUBSCRIPTION_REQUIRED_RETAIN_ALL,

    /**
     * 默认模式。不要求订阅者即可存储；无论订阅或处理状态如何，消息始终保留在主题中。
     */
    SUBSCRIPTION_OPTIONAL_RETAIN_ALL

}
