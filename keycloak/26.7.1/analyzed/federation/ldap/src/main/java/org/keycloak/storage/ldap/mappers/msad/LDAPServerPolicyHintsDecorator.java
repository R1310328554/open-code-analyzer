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

package org.keycloak.storage.ldap.mappers.msad;

import javax.naming.NamingException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.LdapContext;

import org.keycloak.storage.ldap.idm.store.ldap.LDAPOperationManager;
import org.keycloak.storage.ldap.mappers.LDAPOperationDecorator;

import org.jboss.logging.Logger;

/**
 * MSAD 密码策略提示装饰器：在 LDAP 修改密码操作前附加 LDAP_SERVER_POLICY_HINTS 控制，
 * 使 Active Directory 应用密码历史、最短使用期等高级策略。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPServerPolicyHintsDecorator implements LDAPOperationDecorator {

    private static final Logger logger = Logger.getLogger(LDAPServerPolicyHintsDecorator.class);

    /** 较新的 LDAP 密码策略提示 OID（MSAD 2012+）。 */
    public static final String LDAP_SERVER_POLICY_HINTS_OID = "1.2.840.113556.1.4.2239";
    /** 已弃用但仍兼容 MSAD 2008 R2 的 OID。 */
    public static final String LDAP_SERVER_POLICY_HINTS_DEPRECATED_OID = "1.2.840.113556.1.4.2066";

    @Override
    public void beforeLDAPOperation(LdapContext ldapContext, LDAPOperationManager.LdapOperation ldapOperation) throws NamingException {
        logger.debug("Applying LDAP_PASSWORD_POLICY_HINTS_OID before update password");

        final byte[] controlData = {48, (byte) 132, 0, 0, 0, 3, 2, 1, 1};

        // 使用已弃用 OID，因 MSAD 2008 R2 起可用；较新 OID 需 MSAD 2012+
        BasicControl control = new BasicControl(LDAP_SERVER_POLICY_HINTS_DEPRECATED_OID, true, controlData);
        BasicControl[] controls = new BasicControl[] { control };
        ldapContext.setRequestControls(controls);
    }
}
