/*
 *  Copyright 2016 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.keycloak.authorization.config;

import org.keycloak.models.KeycloakSession;
import org.keycloak.wellknown.WellKnownProvider;

/**
 * UMA Well-Known 提供者：暴露 {@link UmaConfiguration} 发现文档。
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class UmaWellKnownProvider implements WellKnownProvider {

    private final KeycloakSession session;

    /** @param session Keycloak 会话 */
    public UmaWellKnownProvider(KeycloakSession session) {
        this.session = session;
    }

    /** @return UMA 2.0 配置 JSON 对象 */
    @Override
    public Object getConfig() {
        return UmaConfiguration.create(session);
    }

    /** 无资源需释放。 */
    @Override
    public void close() {

    }
}
