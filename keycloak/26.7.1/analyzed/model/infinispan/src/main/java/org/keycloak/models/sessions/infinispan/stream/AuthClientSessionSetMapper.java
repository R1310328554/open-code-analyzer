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

package org.keycloak.models.sessions.infinispan.stream;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.keycloak.models.sessions.infinispan.entities.UserSessionEntity;

import org.infinispan.CacheStream;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * {@link CacheStream} 映射函数：从 {@link UserSessionEntity} 关联的客户端会话中提取客户端 ID 集合。
 * <p>
 * 通过 ProtoStream 序列化，供分布式流处理使用。
 */
@ProtoTypeId(Marshalling.AUTHENTICATION_CLIENT_SESSION_KEY_SET_MAPPER)
public class AuthClientSessionSetMapper implements Function<Map.Entry<String, SessionEntityWrapper<UserSessionEntity>>, Set<String>> {

    private static final AuthClientSessionSetMapper INSTANCE = new AuthClientSessionSetMapper();

    private AuthClientSessionSetMapper() {
    }

    /** ProtoStream 工厂方法，返回单例。 */
    @ProtoFactory
    public static AuthClientSessionSetMapper getInstance() {
        return INSTANCE;
    }

    /** 返回用户会话实体中记录的客户端 ID 集合。 */
    @Override
    public Set<String> apply(Map.Entry<String, SessionEntityWrapper<UserSessionEntity>> entry) {
        return entry.getValue().getEntity().getClientSessions();
    }
}
