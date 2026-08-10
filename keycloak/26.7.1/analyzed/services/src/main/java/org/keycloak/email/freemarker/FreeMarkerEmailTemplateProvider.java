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

package org.keycloak.email.freemarker;

import java.io.IOException;
import java.text.Bidi;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import jakarta.enterprise.context.ContextNotActiveException;

import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailSenderProvider;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.email.freemarker.beans.EventBean;
import org.keycloak.email.freemarker.beans.ProfileBean;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.freemarker.model.UrlBean;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.LinkExpirationFormatterMethod;
import org.keycloak.theme.beans.MessageFormatterMethod;
import org.keycloak.theme.freemarker.FreeMarkerProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 FreeMarker 的 {@link EmailTemplateProvider} 默认实现。
 * <p>解析 EMAIL 主题模板与国际化消息，渲染 text/html 正文后委托 {@link EmailSenderProvider} 发送。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class FreeMarkerEmailTemplateProvider implements EmailTemplateProvider {

    private static final Logger log = LoggerFactory.getLogger(FreeMarkerEmailTemplateProvider.class);
    /** 当前 Keycloak 会话。 */
    protected KeycloakSession session;
    /**
     * 认证会话；部分邮件（如定时任务触发）可能为 {@code null}，仅在认证流程内发送（邮箱验证、密码重置、Broker 关联等）时填充。
     */
    protected AuthenticationSessionModel authenticationSession;
    /** FreeMarker 模板引擎提供者。 */
    protected FreeMarkerProvider freeMarker;
    /** 目标 Realm，用于 SMTP 配置与主题资源。 */
    protected RealmModel realm;
    /** 收件用户。 */
    protected UserModel user;
    /** 附加模板属性（可通过 {@link #setAttribute} 注入）。 */
    protected final Map<String, Object> attributes = new HashMap<>();

    /** @param session 当前会话 */
    public FreeMarkerEmailTemplateProvider(KeycloakSession session) {
        this.session = session;
        this.freeMarker = session.getProvider(FreeMarkerProvider.class);
    }

    @Override
    /** 设置目标 Realm。 */
    public EmailTemplateProvider setRealm(RealmModel realm) {
        this.realm = realm;
        return this;
    }

    @Override
    /** 设置收件用户。 */
    public EmailTemplateProvider setUser(UserModel user) {
        this.user = user;
        return this;
    }

    @Override
    /** 向模板上下文追加自定义属性。 */
    public EmailTemplateProvider setAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    @Override
    /** 绑定当前认证会话供模板使用。 */
    public EmailTemplateProvider setAuthenticationSession(AuthenticationSessionModel authenticationSession) {
        this.authenticationSession = authenticationSession;
        return this;
    }

    /** @return Realm 显示名，缺省时为首字母大写的内部名 */
    protected String getRealmName() {
        if (realm.getDisplayName() != null) {
            return realm.getDisplayName();
        } else {
            return ObjectUtil.capitalize(realm.getName());
        }
    }

    @Override
    /** 按 {@link EventType} 选择 event-*.ftl 模板并发送用户事件通知邮件。 */
    public void sendEvent(Event event) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        attributes.put("event", new EventBean(event));

        send(toCamelCase(event.getType()) + "Subject", "event-" + event.getType().toString().toLowerCase() + ".ftl", attributes);
    }

    @Override
    /** 发送密码重置链接邮件。 */
    public void sendPasswordReset(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        send("passwordResetSubject", "password-reset.ftl", attributes);
    }

    @Override
    /** 使用指定 SMTP 配置向用户发送连通性测试邮件。 */
    public void sendSmtpTestEmail(Map<String, String> config, UserModel user) throws EmailException {
        setRealm(session.getContext().getRealm());
        setUser(user);

        Map<String, Object> attributes = new HashMap<>(this.attributes);

        EmailTemplate email = processTemplate("emailTestSubject", Collections.emptyList(), "email-test.ftl", attributes);
        send(config, email.getSubject(), email.getTextBody(), email.getHtmlBody());
    }

    @Override
    /** 发送身份代理账户关联确认邮件。 */
    public void sendConfirmIdentityBrokerLink(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        BrokeredIdentityContext brokerContext = (BrokeredIdentityContext) this.attributes.get(IDENTITY_PROVIDER_BROKER_CONTEXT);
        String idpAlias = brokerContext.getIdpConfig().getAlias();
        String idpDisplayName = brokerContext.getIdpConfig().getDisplayName();
        if (ObjectUtil.isBlank(idpDisplayName)) {
            idpDisplayName = ObjectUtil.capitalize(idpAlias);
        }

        attributes.put("identityProviderContext", brokerContext);
        attributes.put("identityProviderAlias", idpAlias);
        attributes.put("identityProviderDisplayName", idpDisplayName);

        List<Object> subjectAttrs = Collections.singletonList(idpDisplayName);
        send("identityProviderLinkSubject", subjectAttrs, "identity-provider-link.ftl", attributes);
    }

    @Override
    /** 发送「执行必需操作」邮件（如强制改密）。 */
    public void sendExecuteActions(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        send("executeActionsSubject", "executeActions.ftl", attributes);
    }

    @Override
    /** 发送可验证凭证发放邀请邮件。 */
    public void sendVerifiableCredentialOffer(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        send("verifiableCredentialOfferSubject", "verifiable-credential-offer.ftl", attributes);
    }

    @Override
    /** 发送邮箱地址验证邮件。 */
    public void sendVerifyEmail(String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);

        send("emailVerificationSubject", "email-verification.ftl", attributes);
    }

    @Override
    /** 发送组织邀请邮件。 */
    public void sendOrgInviteEmail(OrganizationModel organization, String link, long expirationInMinutes) throws EmailException {
        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);
        attributes.put("organization", organization);
        if (user.getFirstName() != null && user.getLastName() != null) {
            attributes.put("firstName", user.getFirstName());
            attributes.put("lastName", user.getLastName());
        }
        send("orgInviteSubject", List.of(organization.getName()), "org-invite.ftl", attributes);
    }

    @Override
    /** 向新邮箱地址发送邮箱变更确认邮件。 */
    public void sendEmailUpdateConfirmation(String link, long expirationInMinutes, String newEmail) throws EmailException {
        if (newEmail == null) {
            throw new IllegalArgumentException("The new email is mandatory");
        }

        Map<String, Object> attributes = new HashMap<>(this.attributes);
        addLinkInfoIntoAttributes(link, expirationInMinutes, attributes);
        attributes.put("newEmail", newEmail);

        send("emailUpdateConfirmationSubject", Collections.emptyList(), "email-update-confirmation.ftl", attributes, newEmail);
    }

    /**
     * 向模板属性注入操作链接与过期时间格式化器。
     *
     * @param link 用户操作 URL
     * @param expirationInMinutes 链接有效分钟数
     * @param attributes 待填充的模板属性映射
     */
    protected void addLinkInfoIntoAttributes(String link, long expirationInMinutes, Map<String, Object> attributes) throws EmailException {
        attributes.put("link", link);
        attributes.put("linkExpiration", expirationInMinutes);
        try {
            Locale locale = session.getContext().resolveLocale(user, Boolean.parseBoolean(String.valueOf(attributes.get(Constants.IGNORE_ACCEPT_LANGUAGE_HEADER))));
            attributes.put("linkExpirationFormatter", new LinkExpirationFormatterMethod(getTheme().getMessages(locale), locale));
        } catch (IOException e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    @Override
    public void send(String subjectFormatKey, String bodyTemplate, Map<String, Object> bodyAttributes) throws EmailException {
        send(subjectFormatKey, Collections.emptyList(), bodyTemplate, bodyAttributes);
    }

    /** 解析主题消息键、渲染 text/html FreeMarker 模板并返回 {@link EmailTemplate}。 */
    protected EmailTemplate processTemplate(String subjectKey, List<Object> subjectAttributes, String template, Map<String, Object> attributes) throws EmailException {
        try {
            Locale locale = session.getContext().resolveLocale(user, Boolean.parseBoolean(String.valueOf(attributes.get(Constants.IGNORE_ACCEPT_LANGUAGE_HEADER))));
            attributes.put("locale", locale);

            Theme theme = getTheme();
            Properties messages = theme.getEnhancedMessages(realm, locale);

            String currentLanguageTag = locale.getLanguage();
            String currentLanguage = messages.getProperty("locale_" + currentLanguageTag, currentLanguageTag);
            boolean isLtr = new Bidi(currentLanguage, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).isLeftToRight();
            attributes.put("ltr", isLtr);

            attributes.put("msg", new MessageFormatterMethod(locale, messages));

            attributes.put("properties", theme.getProperties());
            attributes.put("realmName", getRealmName());
            attributes.put("user", new ProfileBean(user, session));

            try {
                KeycloakUriInfo uriInfo = session.getContext().getUri();
                attributes.put("url", new UrlBean(realm, theme, uriInfo.getBaseUri(), null));
            } catch (ContextNotActiveException e) {
                log.debug("No active request, can't make url attribute available to the template");
                // 无活跃请求上下文（如定时任务发信）时忽略，模板中 url 属性不可用
            }

            String subject = new MessageFormat(messages.getProperty(subjectKey, subjectKey), locale).format(subjectAttributes.toArray());
            attributes.put("subject", subject);
            String textTemplate = String.format("text/%s", template);
            String textBody;
            try {
                textBody = freeMarker.processTemplate(attributes, textTemplate, theme);
            } catch (final FreeMarkerException e) {
                throw new EmailException("Failed to template plain text email.", e);
            }
            String htmlTemplate = String.format("html/%s", template);
            String htmlBody;
            try {
                htmlBody = freeMarker.processTemplate(attributes, htmlTemplate, theme);
            } catch (final FreeMarkerException e) {
                throw new EmailException("Failed to template html email.", e);
            }

            return new EmailTemplate(subject, textBody, htmlBody);
        } catch (Exception e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    /** @return EMAIL 类型主题 */
    protected Theme getTheme() throws IOException {
        return session.theme().getTheme(Theme.Type.EMAIL);
    }

    @Override
    public void send(String subjectFormatKey, List<Object> subjectAttributes, String bodyTemplate, Map<String, Object> bodyAttributes) throws EmailException {
        send(subjectFormatKey, subjectAttributes, bodyTemplate, bodyAttributes, null);
    }

    @Override
    public void send(String subjectFormatKey, String bodyTemplate, Map<String, Object> bodyAttributes, String destinationEmail) throws EmailException {
        send(subjectFormatKey, Collections.emptyList(), bodyTemplate, bodyAttributes, destinationEmail);
    }

    @Override
    public void send(String subjectFormatKey, List<Object> subjectAttributes, String bodyTemplate,
        Map<String, Object> bodyAttributes, String address) throws EmailException {
        try {
            EmailTemplate email = processTemplate(subjectFormatKey, subjectAttributes, bodyTemplate, bodyAttributes);
            send(email.getSubject(), email.getTextBody(), email.getHtmlBody(), address);
        } catch (EmailException e) {
            throw e;
        } catch (Exception e) {
            throw new EmailException("Failed to template email", e);
        }
    }

    protected void send(String subject, String textBody, String htmlBody, String address) throws EmailException {
        send(realm.getSmtpConfig(), subject, textBody, htmlBody, address);
    }

    protected void send(Map<String, String> config, String subject, String textBody, String htmlBody) throws EmailException {
        send(config, subject, textBody, htmlBody, null);
    }

    /** 委托 {@link EmailSenderProvider} 发送已渲染的邮件。 */
    protected void send(Map<String, String> config, String subject, String textBody, String htmlBody, String address) throws EmailException {
        EmailSenderProvider emailSender = session.getProvider(EmailSenderProvider.class);
        if (address == null) {
            emailSender.send(config, user, subject, textBody, htmlBody);
        } else {
            emailSender.send(config, address, subject, textBody, htmlBody);
        }
    }

    @Override
    public void close() {
    }

    /** 将 {@link EventType} 枚举名转为 camelCase 消息键前缀（如 eventLoginError）。 */
    protected String toCamelCase(EventType event) {
        StringBuilder sb = new StringBuilder("event");
        for (String s : event.name().toLowerCase().split("_")) {
            sb.append(ObjectUtil.capitalize(s));
        }
        return sb.toString();
    }

    /** 已渲染邮件的主题与 text/html 正文容器。 */
    protected static class EmailTemplate {

        private String subject;
        private String textBody;
        private String htmlBody;

        /** @param subject 邮件主题 @param textBody 纯文本正文 @param htmlBody HTML 正文 */
        public EmailTemplate(String subject, String textBody, String htmlBody) {
            this.subject = subject;
            this.textBody = textBody;
            this.htmlBody = htmlBody;
        }

        public String getSubject() {
            return subject;
        }

        public String getTextBody() {
            return textBody;
        }

        public String getHtmlBody() {
            return htmlBody;
        }
    }

}
