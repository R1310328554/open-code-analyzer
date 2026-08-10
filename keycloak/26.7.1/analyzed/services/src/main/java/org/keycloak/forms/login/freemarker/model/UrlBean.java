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

import java.io.IOException;
import java.net.URI;

import org.keycloak.models.RealmModel;
import org.keycloak.services.Urls;
import org.keycloak.theme.Theme;

import org.jboss.logging.Logger;

import static org.keycloak.protocol.oidc.grants.device.DeviceGrantType.realmOAuth2DeviceVerificationAction;

/**
 * 登录页 URL 构建 Bean：向 FreeMarker 模板暴露登录、注册、重置凭证、主题资源等链接。
 * <p>模板中通过 {@code url} 变量访问各 {@code get*} 方法。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class UrlBean {

    private static final Logger logger = Logger.getLogger(UrlBean.class);
    private final URI actionuri;
    private URI baseURI;
    private Theme theme;
    private String realm;
    private URI themeRootUri;

    /** @param realm 当前领域 @param theme 登录主题 @param baseURI 基准 URI @param actionUri 当前表单 action URI */
    public UrlBean(RealmModel realm, Theme theme, URI baseURI, URI actionUri) {
        this.realm = realm != null ? realm.getName() : null;
        this.theme = theme;
        this.baseURI = baseURI;
        this.actionuri = actionUri;
    }

    /** @return 当前登录表单 action URL；未设置 action 时抛异常 */
    public String getLoginAction() {
        if (this.actionuri != null) {
            return this.actionuri.toString();
        }
        throw new RuntimeException("action URI not set");
    }

    /** @return 领域登录页 URL */
    public String getLoginUrl() {
        return Urls.realmLoginPage(baseURI, realm).toString();
    }

    /** @return 重启认证流 URL（不保留 SSO 会话） */
    public String getLoginRestartFlowUrl() {
        return Urls.realmLoginRestartPage(baseURI, realm, false).toString();
    }

    /** @return 在其他标签页继续 SSO 登录的 URL */
    public String getSsoLoginInOtherTabsUrl() {
        return Urls.realmLoginRestartPage(baseURI, realm, true).toString();
    }

    /** @return 是否已设置 action URI */
    public boolean hasAction()  {
        return actionuri != null;
    }

    /** @return 注册表单 action URL；无 action 时返回默认注册 action */
    public String getRegistrationAction() {
        if (this.actionuri != null) {
            return this.actionuri.toString();
        }
        return Urls.realmRegisterAction(baseURI, realm).toString();
    }

    /** @return 注册页 URL */
    public String getRegistrationUrl() {
        return Urls.realmRegisterPage(baseURI, realm).toString();
    }

    /** @return 重置凭证页 URL */
    public String getLoginResetCredentialsUrl() {
        return Urls.loginResetCredentials(baseURI, realm).toString();
    }

    /** @return 用户名提醒页 URL */
    public String getLoginUsernameReminderUrl() {
        return Urls.loginUsernameReminder(baseURI, realm).toString();
    }

    /** @return 首次 IdP 关联登录处理 URL */
    public String getFirstBrokerLoginUrl() {
        return Urls.firstBrokerLoginProcessor(baseURI, realm).toString();
    }

    /** @return 登出确认 action URL */
    public String getLogoutConfirmAction() {
        return Urls.logoutConfirm(baseURI, realm).toString();
    }

    /** @return 主题资源完整 URL（含 type/name） */
    public String getResourcesUrl() {
        return getThemeRootUri().toString() + "/" + theme.getType().toString().toLowerCase() +"/" + theme.getName();
    }

    /** @return OAuth 授权 action 路径或 URL */
    public String getOauthAction() {
        if (this.actionuri != null) {
            return this.actionuri.getPath();
        }

        return Urls.realmOauthAction(baseURI, realm).toString();
    }

    /** @return OAuth2 设备授权验证 action 路径或 URL */
    public String getOauth2DeviceVerificationAction() {
        if (this.actionuri != null) {
            return this.actionuri.getPath();
        }

        return realmOAuth2DeviceVerificationAction(baseURI, realm).toString();
    }

    /** @return 主题资源相对路径 */
    public String getResourcesPath() {
        URI uri = getThemeRootUri();
        return uri.getPath() + "/" + theme.getType().toString().toLowerCase() +"/" + theme.getName();
    }

    /** @return 主题公共资源相对路径 */
    public String getResourcesCommonPath() {
        URI uri = getThemeRootUri();
        return uri.getPath() + "/" + getCommonPath();
    }

    /** @return 主题公共资源完整 URL */
    public String getResourcesCommonUrl() {
        return getThemeRootUri().toString() + "/" + getCommonPath();
    }

    /** 从 theme.properties 读取 common 路径，默认 {@code common/keycloak}。 */
    private String getCommonPath() {
        String commonPath = "";
        try {
            commonPath = theme.getProperties().getProperty("common");
        } catch (IOException ex) {
            logger.warn("Failed to load properties", ex);
        }
        if (commonPath == null || commonPath.isEmpty()) {
            commonPath = "common/keycloak";
        }
        return commonPath;
    }

    /** 懒加载主题根 URI。 */
    private URI getThemeRootUri() {
        if (themeRootUri == null) {
            themeRootUri = Urls.themeRoot(baseURI);
        }
        return themeRootUri;
    }
}
