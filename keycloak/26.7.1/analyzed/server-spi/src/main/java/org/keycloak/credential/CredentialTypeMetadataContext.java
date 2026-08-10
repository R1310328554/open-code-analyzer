/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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
 *
 */

package org.keycloak.credential;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * 构建 {@link CredentialTypeMetadata} 时的上下文（如目标用户）。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CredentialTypeMetadataContext {

    private UserModel user;

    private CredentialTypeMetadataContext() {
    }

    /**
     * @return 元数据关联的用户，可为 null
     * @return user, for which we create metadata. Could be null
     */
    public UserModel getUser() {
        return user;
    }

    /** @return 上下文构建器 */
    public static CredentialTypeMetadataContext.CredentialTypeMetadataContextBuilder builder() {
        return new CredentialTypeMetadataContext.CredentialTypeMetadataContextBuilder();
    }

    // 构建器
    // BUILDER

    public static class CredentialTypeMetadataContextBuilder {

        private CredentialTypeMetadataContext instance = new CredentialTypeMetadataContext();

        public CredentialTypeMetadataContext.CredentialTypeMetadataContextBuilder user(UserModel user) {
            instance.user = user;
            return this;
        }

        /** 构建上下文；用户可为 null。 */
        public CredentialTypeMetadataContext build(KeycloakSession session) {
            // 允许 user 为 null
            // Possible to have null user
            return instance;
        }

    }
}
