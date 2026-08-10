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

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.keycloak.models.sessions.infinispan.entities.SessionEntity;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 缓存流过滤谓词：匹配指定 realm 下仍带 {@link SessionEntityWrapper} 包装的会话条目。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@ProtoTypeId(Marshalling.SESSION_WRAPPER_PREDICATE)
public class SessionWrapperPredicate<K, S extends SessionEntity> extends BaseRealmPredicate<K, SessionEntityWrapper<S>> {

    /** ProtoStream 工厂构造，绑定目标 realm。 */
    @ProtoFactory
    SessionWrapperPredicate(String realmId) {
        super(realmId);
    }

    /** 创建限定 realm 的包装会话谓词。 */
    public static <K1, T extends SessionEntity> SessionWrapperPredicate<K1, T> create(String realm) {
        return new SessionWrapperPredicate<>(realm);
    }

    /** 从包装器内嵌实体读取 realm ID 进行匹配。 */
    @Override
    String realmIdFrom(SessionEntityWrapper<S> value) {
        return value.getEntity().getRealmId();
    }

}
