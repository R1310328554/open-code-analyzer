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

package org.keycloak.services.clientregistration;

import org.keycloak.models.KeycloakSession;
import org.keycloak.representations.idm.ClientRepresentation;

/**
 * 动态客户端注册操作上下文接口。
 * <p>向策略与提供者传递会话、客户端表示及注册提供者引用。</p>
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface ClientRegistrationContext {

    /** @return 当前操作的客户端表示 */
    ClientRepresentation getClient();

    /** @return Keycloak 会话 */
    KeycloakSession getSession();

    /** @return 执行注册的提供者实例 */
    ClientRegistrationProvider getProvider();

}
