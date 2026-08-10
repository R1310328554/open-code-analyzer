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

package org.keycloak.email;

import java.util.List;
import java.util.Map;

import org.keycloak.events.Event;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * 邮件模板渲染与发送提供者 SPI。
 * <p>基于 FreeMarker 模板与国际化消息键生成主题/正文，再委托 {@link EmailSenderProvider} 投递。</p>
 * <p>采用流式 {@code setXxx} 方法链配置认证会话、领域、用户与模板属性。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface EmailTemplateProvider extends Provider {

    /** 身份代理（Broker）上下文在模板属性中的键名。 */
    String IDENTITY_PROVIDER_BROKER_CONTEXT = "identityProviderBrokerCtx";
    
    /** 绑定当前认证会话，供模板读取流程上下文。 */
    EmailTemplateProvider setAuthenticationSession(AuthenticationSessionModel authenticationSession);

    /** 设置目标领域，用于解析 SMTP 配置与主题资源。 */
    EmailTemplateProvider setRealm(RealmModel realm);

    /** 设置收件用户，默认向其邮箱地址发送。 */
    EmailTemplateProvider setUser(UserModel user);

    /** 向模板上下文追加自定义属性。 */
    EmailTemplateProvider setAttribute(String name, Object value);

    /** 根据 {@link Event} 类型选择对应模板并发送通知邮件。 */
    void sendEvent(Event event) throws EmailException;

    /**
     * 发送「忘记密码」重置链接邮件（登录页触发）。
     *
     * @param link 含 action token 的重置 URL
     * @param expirationInMinutes 链接有效分钟数
     * @throws EmailException 渲染或投递失败
     */
    void sendPasswordReset(String link, long expirationInMinutes) throws EmailException;

    /**
     * 使用给定 SMTP 配置向当前用户发送测试邮件，验证连通性。
     *
     * @param config SMTP 服务器配置键值对
     * @param user 测试收件人
     * @throws EmailException 连接或发送失败
     */
    void sendSmtpTestEmail(Map<String, String> config, UserModel user) throws EmailException;

    /**
     * 发送身份代理账户关联确认邮件。
     *
     * @param link 确认关联的操作链接
     * @param expirationInMinutes 链接有效分钟数
     * @throws EmailException 渲染或投递失败
     */
    void sendConfirmIdentityBrokerLink(String link, long expirationInMinutes) throws EmailException;

    /**
     * 发送管理员触发的「执行必需操作」（如修改密码）邮件。
     *
     * @param link 用户执行操作的 URL
     * @param expirationInMinutes 链接有效分钟数
     * @throws EmailException 渲染或投递失败
     */
    void sendExecuteActions(String link, long expirationInMinutes) throws EmailException;

    /** 发送可验证凭证（OID4VCI）发放邀请邮件。 */
    void sendVerifiableCredentialOffer(String link, long expirationInMinutes) throws EmailException;

    /** 发送邮箱地址验证邮件。 */
    void sendVerifyEmail(String link, long expirationInMinutes) throws EmailException;

    /** 发送组织邀请邮件。 */
    void sendOrgInviteEmail(OrganizationModel organization, String link, long expirationInMinutes) throws EmailException;

    /** 发送邮箱变更确认邮件至新地址。 */
    void sendEmailUpdateConfirmation(String link, long expirationInMinutes, String address) throws EmailException;

    /**
     * 按消息键与 FreeMarker 模板发送格式化邮件（主题无占位参数）。
     *
     * @param subjectFormatKey 用于格式化主题的国际化消息键
     * @param bodyTemplate FreeMarker 模板文件名
     * @param bodyAttributes 填充模板的属性映射
     * @throws EmailException 渲染或投递失败
     */
    void send(String subjectFormatKey, String bodyTemplate, Map<String, Object> bodyAttributes) throws EmailException;

    /**
     * 按消息键与占位参数格式化主题，再渲染正文模板并发送。
     *
     * @param subjectFormatKey 主题国际化消息键
     * @param subjectAttributes 主题格式化参数列表
     * @param bodyTemplate FreeMarker 模板文件名
     * @param bodyAttributes 填充模板的属性映射
     * @throws EmailException 渲染或投递失败
     */
    void send(String subjectFormatKey, List<Object> subjectAttributes, String bodyTemplate, Map<String, Object> bodyAttributes) throws EmailException;
    /**
     * 向指定邮箱发送邮件，忽略 {@link #setUser} 绑定的用户地址。
     *
     * @param subjectFormatKey 主题国际化消息键
     * @param bodyTemplate FreeMarker 模板文件名
     * @param bodyAttributes 填充模板的属性映射
     * @param destinationEmail 目标收件地址
     * @throws EmailException 渲染或投递失败
     */
    void send(String subjectFormatKey, String bodyTemplate, Map<String, Object> bodyAttributes, String destinationEmail) throws EmailException;

    /**
     * 向指定邮箱发送带主题占位参数的格式化邮件。
     *
     * @param subjectFormatKey 主题国际化消息键
     * @param subjectAttributes 主题格式化参数列表
     * @param bodyTemplate FreeMarker 模板文件名
     * @param bodyAttributes 填充模板的属性映射
     * @param destinationEmail 目标收件地址
     * @throws EmailException 渲染或投递失败
     */
    void send(String subjectFormatKey, List<Object> subjectAttributes, String bodyTemplate, Map<String, Object> bodyAttributes, String destinationEmail) throws EmailException;
}
