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

package org.keycloak.models.sessions.infinispan.stream;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureEntity;
import org.keycloak.models.sessions.infinispan.entities.LoginFailureKey;
import org.keycloak.models.sessions.infinispan.entities.UserSessionEntity;

/**
 * 会话缓存流操作中常用的映射器工厂方法集合。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class Mappers {

    /** 从缓存条目解包 {@link UserSessionEntity}。 */
    public static Function<Map.Entry<String, SessionEntityWrapper<UserSessionEntity>>, UserSessionEntity> userSessionEntity() {
        return SessionUnwrapMapper.getInstance();
    }

    /** 从登录失败缓存条目提取 {@link LoginFailureKey}。 */
    public static Function<Map.Entry<LoginFailureKey, SessionEntityWrapper<LoginFailureEntity>>, LoginFailureKey> loginFailureId() {
        return MapEntryToKeyMapper.getInstance();
    }

    /** 将集合转为 Stream；自 26.0 起已弃用，请直接使用 {@link Collection#stream()}。 */
    @Deprecated(since = "26.0", forRemoval = true)
    public static <T> Stream<T> toStream(Collection<T> collection) {
        return collection.stream();
    }

    /** 从用户会话条目映射出已认证客户端会话 ID 集合。 */
    public static Function<Map.Entry<String, SessionEntityWrapper<UserSessionEntity>>, Set<String>> authClientSessionSetMapper() {
        return AuthClientSessionSetMapper.getInstance();
    }


}
