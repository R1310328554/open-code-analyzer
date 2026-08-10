/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.organization.forms.login.freemarker.model;

import org.keycloak.forms.login.freemarker.model.RealmBean;
import org.keycloak.models.RealmModel;

/**
 * 组织感知的领域 FreeMarker Bean：在存在组织公开 IdP 时禁用自助注册链接，引导用户通过组织身份提供者完成注册。
 * <p>继承 {@link RealmBean}，覆盖 {@link #isRegistrationAllowed()} 返回 false。</p>
 */
public class OrganizationAwareRealmBean extends RealmBean {

    /** @param realmModel 领域模型 */
    public OrganizationAwareRealmBean(RealmModel realmModel) {
        super(realmModel);
    }

    @Override
    /** @return 组织登录场景下不允许自助注册 */
    public boolean isRegistrationAllowed() {
        return false;
    }
}
