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
package org.redisson.micronaut.session;

/**
 * 跨节点广播：通知其他 Micronaut 实例 Session 已销毁。
 *
 * @author Nikita Koksharov
 */
public class SessionDestroyedMessage extends AttributeMessage {

    public SessionDestroyedMessage() {
    }

    /** @param nodeId 销毁 Session 的节点
     *  @param sessionId 已销毁 Session ID */
    public SessionDestroyedMessage(String nodeId, String sessionId) {
        super(nodeId, sessionId);
    }
    
}
