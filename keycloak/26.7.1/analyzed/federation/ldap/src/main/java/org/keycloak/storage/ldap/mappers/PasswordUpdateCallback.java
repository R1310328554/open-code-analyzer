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

import org.keycloak.models.ModelException;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.ldap.idm.model.LDAPObject;

/**
 * 密码更新回调：在 LDAP 用户密码变更前后及失败时供映射器介入。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface PasswordUpdateCallback {

    /** 密码写入 LDAP 之前调用，可返回 {@link LDAPOperationDecorator} 装饰后续 LDAP 操作。 */
    LDAPOperationDecorator beforePasswordUpdate(UserModel user, LDAPObject ldapUser, UserCredentialModel password);

    /** 密码成功写入 LDAP 后调用。 */
    void passwordUpdated(UserModel user, LDAPObject ldapUser, UserCredentialModel password);

    /** 密码更新失败时调用，可重新抛出或转换 {@link ModelException}。 */
    void passwordUpdateFailed(UserModel user, LDAPObject ldapUser, UserCredentialModel password, ModelException exception) throws ModelException;
}
