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

package org.keycloak.storage.ldap.mappers;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import org.keycloak.storage.ldap.idm.store.ldap.LDAPOperationManager;

/**
 * LDAP 操作装饰器：在 {@link LDAPOperationManager} 执行 LDAP 操作前注入自定义逻辑。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface LDAPOperationDecorator {

    /**
     * LDAP 操作执行前的回调，可用于修改上下文或附加控制。
     *
     * @param ldapContext 当前 LDAP 上下文
     * @param ldapOperation 即将执行的操作类型
     */
    void beforeLDAPOperation(LdapContext ldapContext, LDAPOperationManager.LdapOperation ldapOperation) throws NamingException;

}
