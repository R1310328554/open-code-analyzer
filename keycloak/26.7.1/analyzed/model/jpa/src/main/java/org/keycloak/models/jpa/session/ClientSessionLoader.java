/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.jpa.session;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.session.PersistentAuthenticatedClientSessionAdapter;

/**
 * 客户端会话惰性加载器：首次访问 {@link org.keycloak.models.UserSessionModel#getAuthenticatedClientSessions()}
 * 时从数据库批量填充 client session 映射。
 */
class ClientSessionLoader implements Consumer<Map<String, AuthenticatedClientSessionModel>> {

    /** 是否已完成加载（保证只执行一次）。 */
    private boolean loaded = false;
    /** 提供 client session 适配器流的工厂。 */
    private final Supplier<Stream<PersistentAuthenticatedClientSessionAdapter>> supplier;

    ClientSessionLoader(Supplier<Stream<PersistentAuthenticatedClientSessionAdapter>> supplier) {
        assert supplier != null;
        this.supplier = supplier;
    }


    @Override
    public void accept(Map<String, AuthenticatedClientSessionModel> clientSessions) {
        if (loaded) {
            return;
        }
        // 按 client ID 填入映射，仅加载一次
        supplier.get().forEach(m -> clientSessions.put(m.getClient().getId(), m));
        loaded = true;
    }
}
