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

/**
 * 负确认（NAck）消息的状态枚举。
 * 用于说明消息被负确认的原因。
 *
 * @author Nikita Koksharov
 *
 */
public enum NAckStatus {

    /**
     * 客户端应用能处理该消息，但业务上拒绝接受。
     * 消息将被移除；若配置了死信队列（DLQ）则移入 DLQ。
     */
    REJECTED,

    /**
     * 客户端应用处理消息失败。
     * 消息将被重新投递；可配置失败消息再次投递前的延迟时长。
     */
    FAILED

}
