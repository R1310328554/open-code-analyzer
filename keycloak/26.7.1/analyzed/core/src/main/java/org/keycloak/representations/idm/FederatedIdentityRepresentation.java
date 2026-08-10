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

package org.keycloak.representations.idm;

/**
 * 用户与外部身份提供者（IdP）之间的联邦身份链接表示。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FederatedIdentityRepresentation {

    /** 身份提供者别名。 */
    protected String identityProvider;
    /** 用户在 IdP 侧的唯一标识。 */
    protected String userId;
    /** 用户在 IdP 侧的用户名。 */
    protected String userName;

    /** @return 身份提供者别名 */
    public String getIdentityProvider() {
        return identityProvider;
    }

    /** @param identityProvider 身份提供者别名 */
    public void setIdentityProvider(String identityProvider) {
        this.identityProvider = identityProvider;
    }

    /** @return IdP 侧用户 ID */
    public String getUserId() {
        return userId;
    }

    /** @param userId IdP 侧用户 ID */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /** @return IdP 侧用户名 */
    public String getUserName() {
        return userName;
    }

    /** @param userName IdP 侧用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
