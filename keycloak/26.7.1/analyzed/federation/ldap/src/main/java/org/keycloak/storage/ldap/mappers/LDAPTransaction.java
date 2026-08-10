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

import java.util.HashSet;
import java.util.Set;

import org.keycloak.models.AbstractKeycloakTransaction;
import org.keycloak.models.ModelValidationException;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;

import org.jboss.logging.Logger;

/**
 * LDAP 写事务：在 Keycloak 事务提交时将用户对 {@link LDAPObject} 的变更批量写回 LDAP。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPTransaction extends AbstractKeycloakTransaction {

    public static final Logger logger = Logger.getLogger(LDAPTransaction.class);

    private final LDAPStorageProvider ldapProvider;
    private final LDAPObject ldapUser;

    // 记录本事务中已更新的属性名
    private final Set<String> updatedAttributes = new HashSet<>();

    /** 绑定 LDAP 提供者与待更新的 LDAP 用户对象。 */
    public LDAPTransaction(LDAPStorageProvider ldapProvider, LDAPObject ldapUser) {
        this.ldapProvider = ldapProvider;
        this.ldapUser = ldapUser;
    }


    /** 提交时将已标记的属性变更写入 LDAP 目录。 */
    @Override
    protected void commitImpl() {
        if (logger.isTraceEnabled()) {
            logger.trace("Transaction commit! Updating LDAP attributes for object " + ldapUser.getDn() + ", attributes: " + ldapUser.getAttributes());
        }
        if (ldapUser.isWaitingForExecutionOnMandatoryAttributesComplete()) {
            throw new ModelValidationException("LDAPObject cannot be committed because some mandatory attributes are not set: "
                    + ldapUser.getMandatoryAttributeNamesRemaining());
        }

        if (!updatedAttributes.isEmpty()) {
            ldapProvider.getLdapIdentityStore().update(ldapUser);
        }
    }


    /** 回滚时丢弃对 LDAP 的待写变更。 */
    @Override
    protected void rollbackImpl() {
        logger.warn("Transaction rollback! Ignoring LDAP updates for object " + ldapUser.getDn());
    }

    /**
     * 标记将在本事务中写回 LDAP 的属性。
     *
     * @param attributeName 模型属性名（例如 "firstName"、"lastName"、"street"）
     */
    public void addUpdatedAttribute(String attributeName) {
        if (ldapUser.getDn() != null) {
            // 仅当 ldapObject 已创建并分配 DN 时才记录属性
            updatedAttributes.add(attributeName);
        }
    }

    /**
     * @param attributeName 模型属性名（例如 "firstName"、"lastName"、"street"）
     * @return 该属性是否在本事务中被更新
     */
    public boolean isAttributeUpdated(String attributeName) {
        return updatedAttributes.contains(attributeName);
    }

    /**
     * 标记将在本事务中写回 LDAP 的必需操作（required action）。
     *
     * @param requiredActionName
     */
    public void addUpdatedRequiredAction(String requiredActionName) {
        updatedAttributes.add("requiredAction(" + requiredActionName + ")");
    }

    /**
     *
     * @param requiredActionName
     * @return 该 required action 是否在本事务中被更新
     */
    public boolean isRequiredActionUpdated(String requiredActionName) {
        return updatedAttributes.contains("requiredAction(" + requiredActionName + ")");
    }

}
