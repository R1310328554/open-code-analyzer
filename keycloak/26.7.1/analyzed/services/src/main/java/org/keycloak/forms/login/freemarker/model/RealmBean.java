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
package org.keycloak.forms.login.freemarker.model;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.keycloak.authentication.actiontoken.idpverifyemail.IdpVerifyAccountLinkActionToken;
import org.keycloak.authentication.actiontoken.resetcred.ResetCredentialsActionToken;
import org.keycloak.authentication.actiontoken.verifyemail.VerifyEmailActionToken;
import org.keycloak.models.RealmModel;
import org.keycloak.representations.idm.CredentialRepresentation;

/**
 * Realm FreeMarker Bean：向登录/注册模板暴露 Realm 名称、显示名、注册/登录策略与操作令牌寿命等。
 * <p>封装 {@link RealmModel} 常用布尔开关与属性，简化 FTL 访问。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class RealmBean {

    /** 底层 Realm 模型。 */
    private RealmModel realm;

    /** @param realmModel Realm 模型 */
    public RealmBean(RealmModel realmModel) {
        realm = realmModel;
    }

    /** @return Realm 名称（标识符） */
    public String getName() {
        return realm.getName();
    }

    /** @return 显示名称，未配置时回退为 {@link #getName()} */
    public String getDisplayName() {
        String displayName = realm.getDisplayName();
        if (displayName != null && displayName.length() > 0) {
            return displayName;
        } else {
            return getName();
        }
    }

    /** @return HTML 显示名称，未配置时回退为 {@link #getDisplayName()} */
    public String getDisplayNameHtml() {
        String displayNameHtml = realm.getDisplayNameHtml();
        if (displayNameHtml != null && displayNameHtml.length() > 0) {
            return displayNameHtml;
        } else {
            return getDisplayName();
        }
    }

    /** @return 是否启用身份联邦 */
    public boolean isIdentityFederationEnabled() {
        return realm.isIdentityFederationEnabled();
    }

    /** @return 是否允许自助注册 */
    public boolean isRegistrationAllowed() {
        return realm.isRegistrationAllowed();
    }

    /** @return 注册时是否以邮箱作为用户名 */
    public boolean isRegistrationEmailAsUsername() {
        return realm.isRegistrationEmailAsUsername();
    }

    /** @return 是否允许使用邮箱登录 */
    public boolean isLoginWithEmailAllowed() {
        return realm.isLoginWithEmailAllowed();
    }

    /** @return 是否允许重复邮箱 */
    public boolean isDuplicateEmailsAllowed() {
        return realm.isDuplicateEmailsAllowed();
    }

    /** @return 是否允许重置密码 */
    public boolean isResetPasswordAllowed() {
        return realm.isResetPasswordAllowed();
    }

    /** @return 是否启用“记住我” */
    public boolean isRememberMe() {
        return realm.isRememberMe();
    }

    /** @return 是否启用国际化 */
    public boolean isInternationalizationEnabled() {
        return realm.isInternationalizationEnabled();
    }

    /** @return 是否允许用户编辑用户名 */
    public boolean isEditUsernameAllowed() {
        return realm.isEditUsernameAllowed();
    }

    /** @return Realm 是否要求密码凭证 */
    public boolean isPassword() {
        return realm.getRequiredCredentialsStream()
                .anyMatch(r -> Objects.equals(r.getType(), CredentialRepresentation.PASSWORD));
    }

    /**
     * @return 用户触发的操作令牌默认寿命（分钟）
     */
    public int getActionTokenGeneratedByUserLifespanMinutes() {
        return (int)TimeUnit.SECONDS.toMinutes(realm.getActionTokenGeneratedByUserLifespan());
    }

    /**
     * @return {@link VerifyEmailActionToken} 寿命（分钟）
     */
    public int getVerifyEmailActionTokenLifespanMinutes() {
        return (int)TimeUnit.SECONDS.toMinutes(realm.getActionTokenGeneratedByUserLifespan(VerifyEmailActionToken.TOKEN_TYPE));
    }

    /**
     * @return {@link ResetCredentialsActionToken} 寿命（分钟）
     */
    public int getResetCredentialsActionTokenLifespanMinutes() {
        return (int)TimeUnit.SECONDS.toMinutes(realm.getActionTokenGeneratedByUserLifespan(ResetCredentialsActionToken.TOKEN_TYPE));
    }

    /**
     * @return {@link IdpVerifyAccountLinkActionToken} 寿命（分钟）
     */
    public int getIdpVerifyAccountLinkActionTokenLifespanMinutes() {
        return (int)TimeUnit.SECONDS.toMinutes(realm.getActionTokenGeneratedByUserLifespan(IdpVerifyAccountLinkActionToken.TOKEN_TYPE));
    }

    /** @return Realm 全部字符串属性 */
    public Map<String, String> getAttributes() {
        return realm.getAttributes();
    }

    /** @param key 属性键 @return 单个 Realm 属性值 */
    public String getAttribute(String key) {
        return realm.getAttribute(key);
    }
}
