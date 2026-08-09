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
package org.redisson.tomcat;

/**
 * 跨 Tomcat 节点广播：通知其他实例有新 Session 创建。
 * <p>继承 {@link AttributeMessage}，携带节点 ID 与 Session ID。
 *
 * @author Nikita Koksharov
 */
public class SessionCreatedMessage extends AttributeMessage {

    public SessionCreatedMessage() {
    }

    /** @param nodeId 创建 Session 的节点
     *  @param sessionId 新 Session ID */
    public SessionCreatedMessage(String nodeId, String sessionId) {
        super(nodeId, sessionId);
    }
    
}
