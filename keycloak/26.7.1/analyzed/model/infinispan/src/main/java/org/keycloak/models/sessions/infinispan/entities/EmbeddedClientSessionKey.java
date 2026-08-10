/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.sessions.infinispan.entities;

import org.keycloak.marshalling.Marshalling;

import org.infinispan.protostream.annotations.Proto;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * {@link AuthenticatedClientSessionEntity} 在嵌入式 {@link org.infinispan.Cache} 中的复合键。
 * <p>
 * 结构与 {@link ClientSessionKey} 相同但类型独立，便于各自演进。
 *
 * @param userSessionId 用户会话 ID
 * @param clientId 客户端 ID
 */
@ProtoTypeId(Marshalling.EMBEDDED_CLIENT_SESSION_KEY)
@Proto
public record EmbeddedClientSessionKey(String userSessionId, String clientId) {

    /** 生成 {@code userSessionId::clientId} 形式的字符串 ID。 */
    public String toId() {
        return userSessionId + "::" + clientId;
    }

}
