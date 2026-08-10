/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.events;

import java.util.Objects;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.connections.infinispan.NodeInfo;
import org.keycloak.models.KeycloakSession;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoReserved;

/**
 * 会话相关集群事件的抽象基类，携带 realm、事件键及来源节点/站点信息。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ProtoReserved(numbers = {3}, names = {"resendingEvent"})
public abstract class SessionClusterEvent implements ClusterEvent {

    /** 目标 realm ID。 */
    private String realmId;
    /** 集群通知分组键，同键事件批量发送。 */
    private String eventKey;
    /** 发起节点所属站点（多站点部署）。 */
    private String siteId;
    /** 发起节点的 Infinispan 节点名。 */
    private String nodeId;


    /** 反射创建事件实例并填充 realm、eventKey 及当前节点信息。 */
    public static <T extends SessionClusterEvent> T createEvent(Class<T> eventClass, String eventKey, KeycloakSession session, String realmId) {
        try {
            T event = eventClass.getDeclaredConstructor().newInstance();
            event.setData(session, eventKey, realmId);
            return event;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /** 从当前会话的 Infinispan 连接填充 realm、eventKey 与节点元数据。 */
    void setData(KeycloakSession session, String eventKey, String realmId) {
        this.realmId = realmId;
        this.eventKey = eventKey;
        NodeInfo nodeInfo = session.getProvider(InfinispanConnectionProvider.class).getNodeInfo();
        this.siteId = nodeInfo.siteName();
        this.nodeId = nodeInfo.nodeName();
    }


    @ProtoField(1)
    public String getRealmId() {
        return realmId;
    }

    void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    @ProtoField(2)
    public String getEventKey() {
        return eventKey;
    }

    void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    @ProtoField(4)
    public String getSiteId() {
        return siteId;
    }

    void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    @ProtoField(5)
    public String getNodeId() {
        return nodeId;
    }

    void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionClusterEvent that = (SessionClusterEvent) o;
        return Objects.equals(realmId, that.realmId) && Objects.equals(eventKey, that.eventKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(realmId, eventKey);
    }

    @Override
    public String toString() {
        String simpleClassName = getClass().getSimpleName();
        return String.format("%s [ realmId=%s ]", simpleClassName, realmId);
    }
}
