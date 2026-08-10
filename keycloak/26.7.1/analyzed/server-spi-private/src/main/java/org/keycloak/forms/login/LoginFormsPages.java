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

package org.keycloak.forms.login;

/**
 * 登录/注册主题可渲染的页面类型枚举。
 * <p>由 {@link LoginFormsProvider} 各 {@code createXxx} 方法映射到 FreeMarker 模板。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public enum LoginFormsPages {

    // 认证流程页面：登录、用户名/密码分步、TOTP、WebAuthn、邮箱验证等
    LOGIN, LOGIN_USERNAME, LOGIN_PASSWORD, LOGIN_TOTP, LOGIN_CONFIG_TOTP, LOGIN_WEBAUTHN, LOGIN_VERIFY_EMAIL,
    // 身份代理（IdP）账户关联确认
    LOGIN_IDP_LINK_CONFIRM, LOGIN_IDP_LINK_CONFIRM_OVERRIDE, LOGIN_IDP_LINK_EMAIL,
    // OAuth 同意、密码重置/更新、注册、信息/错误页
    OAUTH_GRANT, LOGIN_RESET_PASSWORD, LOGIN_UPDATE_PASSWORD, LOGIN_SELECT_AUTHENTICATOR, REGISTER, INFO, ERROR, ERROR_WEBAUTHN, LOGIN_UPDATE_PROFILE,
    // 会话过期、授权码、X509/SAML 专用页
    LOGIN_PAGE_EXPIRED, CODE, X509_CONFIRM, SAML_POST_FORM,
    // OAuth2 设备码验证、IdP 资料审核
    LOGIN_OAUTH2_DEVICE_VERIFY_USER_CODE, IDP_REVIEW_USER_PROFILE,
    // 恢复认证码输入与配置
    LOGIN_RECOVERY_AUTHN_CODES_INPUT, LOGIN_RECOVERY_AUTHN_CODES_CONFIG,
    // 前端通道登出、登出确认、邮箱更新、OTP 重置
    FRONTCHANNEL_LOGOUT, LOGOUT_CONFIRM, UPDATE_EMAIL, LOGIN_RESET_OTP;

}
