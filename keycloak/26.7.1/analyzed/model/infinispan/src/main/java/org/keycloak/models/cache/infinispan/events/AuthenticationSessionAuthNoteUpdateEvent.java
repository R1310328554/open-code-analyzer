/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.models.cache.infinispan.events;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.marshalling.Marshalling;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 认证会话 auth note 更新的集群广播事件。
 * <p>
 * 当某浏览器标签页的认证会话 auth note 发生变更时，通过 {@link ClusterEvent} 在集群节点间同步片段数据，
 * 使各节点缓存的认证上下文保持一致。
 *
 * @author hmlnarik
 */
@ProtoTypeId(Marshalling.AUTHENTICATION_SESSION_AUTH_NOTE_UPDATE_EVENT)
public class AuthenticationSessionAuthNoteUpdateEvent implements ClusterEvent {

    /** 认证会话 ID。 */
    private final String authSessionId;
    /** 浏览器标签页 ID，区分同一会话的多标签上下文。 */
    private final String tabId;
    /** auth note 键值片段（非线程安全，当前用法可接受）。 */
    private final Map<String, String> authNotesFragment;

    /** 私有构造函数，通过 {@link #create} 工厂方法实例化。 */
    private AuthenticationSessionAuthNoteUpdateEvent(Map<String, String> authNotesFragment, String authSessionId, String tabId) {
        this.authNotesFragment = Objects.requireNonNull(authNotesFragment);
        this.authSessionId = Objects.requireNonNull(authSessionId);
        this.tabId = Objects.requireNonNull(tabId);
    }

    /**
     * 创建 auth note 更新事件实例。
     *
     * @param authSessionId 认证会话 ID
     * @param tabId 浏览器标签页 ID
     * @param authNotesFragment auth note 键值片段
     * @return 事件实例；注意 {@code authNotesFragment} 非线程安全，当前场景可接受
     */
    @ProtoFactory
    public static AuthenticationSessionAuthNoteUpdateEvent create(String authSessionId, String tabId, Map<String, String> authNotesFragment) {
        return new AuthenticationSessionAuthNoteUpdateEvent(authNotesFragment, authSessionId, tabId);
    }

    /** 返回认证会话 ID。 */
    @ProtoField(1)
    public String getAuthSessionId() {
        return authSessionId;
    }

    /** 返回浏览器标签页 ID。 */
    @ProtoField(2)
    public String getTabId() {
        return tabId;
    }

    /** 返回 auth note 键值片段映射。 */
    @ProtoField(value = 3, mapImplementation = LinkedHashMap.class)
    public Map<String, String> getAuthNotesFragment() {
        return authNotesFragment;
    }

    /** 基于会话 ID 与标签页 ID 判断相等性。 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AuthenticationSessionAuthNoteUpdateEvent that = (AuthenticationSessionAuthNoteUpdateEvent) o;
        return Objects.equals(authSessionId, that.authSessionId) && Objects.equals(tabId, that.tabId);
    }

    /** 返回基于会话 ID 与标签页 ID 的哈希值。 */
    @Override
    public int hashCode() {
        return Objects.hash(authSessionId, tabId);
    }

    /** 返回便于调试的字符串表示。 */
    @Override
    public String toString() {
        return String.format("AuthenticationSessionAuthNoteUpdateEvent [ authSessionId=%s, tabId=%s, authNotesFragment=%s ]",
                authSessionId, tabId, authNotesFragment);
    }

}
