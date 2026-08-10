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

package org.keycloak.storage.ldap.idm.store.ldap;

import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;

import org.keycloak.models.KeycloakSession;

/**
 * 与会话绑定的 {@link InitialLdapContext}，在 {@link KeycloakSession} 关闭时自动释放 LDAP 资源。
 */
public final class SessionBoundInitialLdapContext extends InitialLdapContext {

    /**
     * @param session Keycloak 会话
     * @param environment JNDI 环境属性
     * @param connCtls 连接控制
     */
    public SessionBoundInitialLdapContext(KeycloakSession session, Hashtable<?, ?> environment, Control[] connCtls) throws NamingException {
        super(environment, connCtls);
        session.enlistForClose(() -> {
                try {
                    close();
                } catch (NamingException e) {
                    failedToCloseLdapContext(e);
                }
        });
    }

    /** LDAP 上下文关闭失败时抛出运行时异常。 */
    private void failedToCloseLdapContext(NamingException e) {
        throw new RuntimeException("Failed to close LDAP context", e);
    }
}
