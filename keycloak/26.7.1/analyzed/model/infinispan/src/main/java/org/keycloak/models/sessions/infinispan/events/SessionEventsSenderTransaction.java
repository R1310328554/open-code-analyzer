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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.cluster.ClusterEvent;
import org.keycloak.cluster.ClusterProvider;
import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.KeycloakSession;

/**
 * 将会话集群事件的发送推迟到 Keycloak 事务提交时批量通知。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SessionEventsSenderTransaction extends AbstractKeycloakTransaction {

    private final KeycloakSession session;

    /** 按 eventKey 分组待发送的集群事件列表。 */
    private final Map<String, List<ClusterEvent>> sessionEvents = new HashMap<>();

    public SessionEventsSenderTransaction(KeycloakSession session) {
        this.session = session;
    }

    /** 将事件加入对应 eventKey 的待发送队列。 */
    public void addEvent(SessionClusterEvent event) {
        sessionEvents.computeIfAbsent(event.getEventKey(), eventGroup -> new ArrayList<>()).add(event);
    }

    @Override
    protected void commitImpl() {
        // 提交时按分组键一次性通知集群
        var cluster = session.getProvider(ClusterProvider.class);
        for (var entry : sessionEvents.entrySet()) {
            cluster.notify(entry.getKey(), entry.getValue(), false);
        }
    }


    @Override
    protected void rollbackImpl() {
        // 回滚时丢弃已排队的事件
        sessionEvents.clear();
    }

}
