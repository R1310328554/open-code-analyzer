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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.Provider;
import org.keycloak.rar.AuthorizationDetails;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 登录/注册表单渲染提供者 SPI。
 * <p>基于 FreeMarker 主题模板生成 HTML 响应，供认证流程与 OAuth 同意页使用。 采用流式 {@code setXxx} 方法链配置会话、用户与消息。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface LoginFormsProvider extends Provider {

    /** 更新资料上下文在模板属性中的键名。 */
    String UPDATE_PROFILE_CONTEXT_ATTR = "updateProfileCtx";

    /** 身份代理（Broker）上下文在模板属性中的键名。 */
    String IDENTITY_PROVIDER_BROKER_CONTEXT = "identityProviderBrokerCtx";

    /** 模板中隐藏用户名字段的标志键。 */
    String USERNAME_HIDDEN = "usernameHidden";

    /** 模板中禁用自助注册的标志键。 */
    String REGISTRATION_DISABLED = "registrationDisabled";


    /**
     * 向 HTML 页头追加外部脚本 URL。
     *
     * @param scriptUrl
     */
    void addScript(String scriptUrl);

    /** 为指定必需操作（如更新密码）渲染表单响应。 */
    Response createResponse(UserModel.RequiredAction action);

    /** 按 {@link LoginFormsPages} 名称渲染通用表单。 */
    Response createForm(String form);

    /** 解析国际化消息键并格式化参数。 */
    String getMessage(String message, Object... parameters);

    /** 渲染用户名+密码合并登录页。 */
    Response createLoginUsernamePassword();

    /** 渲染仅用户名步骤登录页。 */
    Response createLoginUsername();

    /** 渲染仅密码步骤登录页。 */
    Response createLoginPassword();

    /** 渲染 OTP 重置页。 */
    Response  createOtpReset();

    /** 渲染忘记密码/重置密码页。 */
    Response createPasswordReset();

    /** 渲染 TOTP 验证码输入页。 */
    Response createLoginTotp();

    /** 渲染恢复认证码输入页。 */
    Response createLoginRecoveryAuthnCode();

    /** 渲染 WebAuthn/Passkey 认证页。 */
    Response createLoginWebAuthn();

    /** 渲染用户自助注册页。 */
    Response createRegistration();

    /** 渲染通用信息提示页。 */
    Response createInfoPage();

    /** 渲染更新用户资料页。 */
    Response createUpdateProfilePage();

    /** 渲染 IdP 账户关联确认页。 */
    Response createIdpLinkConfirmLinkPage();

    /** 渲染 IdP 关联覆盖确认页（已有本地账户时）。 */
    Response createIdpLinkConfirmOverrideLinkPage();

    /** 渲染 IdP 关联邮件验证提示页。 */
    Response createIdpLinkEmailPage();

    /** 渲染登录会话过期页。 */
    Response createLoginExpiredPage();

    /** 渲染带 HTTP 状态的错误页。 */
    Response createErrorPage(Response.Status status);

    /** 渲染 WebAuthn 专用错误页。 */
    Response createWebAuthnErrorPage();

    /** 渲染 OAuth/OIDC 授权同意页。 */
    Response createOAuthGrant();

    /** 渲染多因素认证方式选择页。 */
    Response createSelectAuthenticator();

    /** 渲染 OAuth2 设备授权用户码验证页。 */
    Response createOAuth2DeviceVerifyUserCodePage();

    /** 渲染授权码展示页（如设备流程）。 */
    Response createCode();

    /** 渲染 X509 客户端证书确认页。 */
    Response createX509ConfirmPage();

    /** 渲染 SAML POST 绑定自动提交表单。 */
    Response createSamlPostForm();

    /** 渲染 OIDC 前端通道登出 iframe 页。 */
    Response createFrontChannelLogoutPage();

    /** 渲染登出确认页。 */
    Response createLogoutConfirmPage();

    /** 绑定当前认证会话，供模板读取流程上下文。 */
    LoginFormsProvider setAuthenticationSession(AuthenticationSessionModel authenticationSession);

    /** 设置客户端会话/授权码，供 OAuth 同意页使用。 */
    LoginFormsProvider setClientSessionCode(String accessCode);

    /** 设置客户端请求的授权范围/详情列表。 */
    LoginFormsProvider setAccessRequest(List<AuthorizationDetails> clientScopesRequested);

    /**
     * 设置全局单条错误消息。
     * 
     * @param message key of message
     * @param parameters to be formatted into message
     */
    LoginFormsProvider setError(String message, Object ... parameters);
    
    /**
     * 设置多条错误消息。
     * 
     * @param messages to be set
     */
    LoginFormsProvider setErrors(List<FormMessage> messages);

    /** 追加单条错误消息。 */
    LoginFormsProvider addError(FormMessage errorMessage);

    /**
     * 向表单追加成功消息。
     *
     * @param errorMessage
     * @return
     */
    LoginFormsProvider addSuccess(FormMessage errorMessage);

    /** 设置全局成功消息。 */
    LoginFormsProvider setSuccess(String message, Object ... parameters);

    /** 设置全局信息提示消息。 */
    LoginFormsProvider setInfo(String message, Object ... parameters);

    /** 按 {@link MessageType} 设置带类型的表单消息。 */
    LoginFormsProvider setMessage(MessageType type, String message, Object... parameters);

    /**
     * 认证会话已移除时使用：切换语言后重复渲染先前的信息/错误页，无需会话数据。
     */
    LoginFormsProvider setDetachedAuthSession();

    /** 设置当前用户，供模板显示用户名等。 */
    LoginFormsProvider setUser(UserModel user);

    /** 向 HTTP 响应追加自定义头。 */
    LoginFormsProvider setResponseHeader(String headerName, String headerValue);

    /** 回填表单字段值（如验证失败后保留用户输入）。 */
    LoginFormsProvider setFormData(MultivaluedMap<String, String> formData);

    /** 向模板上下文追加自定义属性。 */
    LoginFormsProvider setAttribute(String name, Object value);

    /** 设置 HTTP 响应状态码。 */
    LoginFormsProvider setStatus(Response.Status status);

    /** 设置表单提交目标 URI。 */
    LoginFormsProvider setActionUri(URI requestUri);

    /** 设置当前认证执行步骤标识。 */
    LoginFormsProvider setExecution(String execution);

    /** 绑定认证流程上下文。 */
    LoginFormsProvider setAuthContext(AuthenticationFlowContext context);

    /** 注册模板属性映射函数，在渲染前变换属性映射。 */
    LoginFormsProvider setAttributeMapper(Function<Map<String, Object>, Map<String, Object>> configurer);
}
