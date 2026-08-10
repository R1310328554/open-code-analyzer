/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.workflow;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.common.util.DurationConverter;
import org.keycloak.common.util.StringPropertyReplacer.PropertyResolver;
import org.keycloak.component.ComponentModel;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.jboss.logging.Logger;

import static org.keycloak.common.util.StringPropertyReplacer.replaceProperties;

/**
 * 通知用户工作流步骤：向用户或指定邮箱发送工作流通知邮件。
 * <p>支持自定义主题/正文、属性占位符替换及下一步骤倒计时；优先发送至 {@code to} 配置地址，否则使用用户邮箱。</p>
 */
public class NotifyUserStepProvider implements WorkflowStepProvider {

    private final KeycloakSession session;
    private final ComponentModel stepModel;
    private final Logger log = Logger.getLogger(NotifyUserStepProvider.class);

    /** @param session Keycloak 会话 @param model 步骤组件配置（含 to/subject/message 等） */
    public NotifyUserStepProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.stepModel = model;
    }

    /** 无资源需释放。 */
    @Override
    public void close() {
    }

    /** 解析收件人、主题与模板属性后发送通知邮件。 */
    @Override
    public void run(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        EmailTemplateProvider emailProvider = session.getProvider(EmailTemplateProvider.class).setRealm(realm);

        String subjectKey = getSubjectKey(context);
        String bodyTemplate = getBodyTemplate();
        Map<String, Object> bodyAttributes = getBodyAttributes(context);
        UserModel user = session.users().getUserById(realm, context.getResourceId());
        
        if (user != null) {
            emailProvider.setUser(user);
        }

        String targetEmail = stepModel.getConfig().getFirst("to");

        if (targetEmail != null && !targetEmail.trim().isEmpty()) {
            try {
                emailProvider.send(subjectKey, bodyTemplate, bodyAttributes, targetEmail);
                log.debugv("Notification email sent to {0}", targetEmail);
            } catch (EmailException e) {
                log.errorv(e, "Failed to send notification email to {0}", targetEmail);
            }
        } else if (user != null && user.getEmail() != null) {
            try {
                emailProvider.send(subjectKey, bodyTemplate, bodyAttributes);
                log.debugv("Notification email sent to user {0} ({1})", user.getUsername(), user.getEmail());
            } catch (EmailException e) {
                log.errorv(e, "Failed to send notification email to user {0} ({1})", user.getUsername(), user.getEmail());
            }
        } else if (user != null && user.getEmail() == null) {
            log.warnv("User {0} has no email address, skipping notification", user.getUsername());
        }
    }

    /** 获取邮件主题键：优先步骤配置，其次下一步骤通知主题，最后默认 accountNotificationSubject。 */
    private String getSubjectKey(WorkflowExecutionContext context) {
        String customSubjectKey = stepModel.getConfig().getFirst("subject");
        
        if (customSubjectKey != null && !customSubjectKey.trim().isEmpty()) {
            return customSubjectKey;
        }

        WorkflowStep nextStep = context.getNextStep();

        if (nextStep == null || nextStep.getNotificationSubject() == null) {
            return "accountNotificationSubject";
        }

        return nextStep.getNotificationSubject();
    }

    /** @return 邮件正文 FreeMarker 模板文件名 */
    private String getBodyTemplate() {
        return "workflow-notification.ftl";
    }

    /** 构建模板变量：消息键、剩余天数、原因、领域名及下一步骤类型等。 */
    private Map<String, Object> getBodyAttributes(WorkflowExecutionContext context) {
        RealmModel realm = session.getContext().getRealm();
        Map<String, Object> attributes = new HashMap<>();
        WorkflowStep nextStep = context.getNextStep();

        // 自定义消息覆盖，或根据下一步骤类型使用默认消息键
        String customMessage = stepModel.getConfig().getFirst("message");
        if (customMessage != null && !customMessage.trim().isEmpty()) {
            attributes.put("messageKey", "customMessage");
            attributes.put("customMessage", replaceProperties(customMessage, new NotificationPropertyResolver(session, context)));
        } else if (nextStep != null && nextStep.getNotificationMessage() != null) {
            attributes.put("messageKey", nextStep.getNotificationMessage());
        } else {
            attributes.put("messageKey", "accountNotificationBody");
        }
        
        // 计算距离下一步骤执行还剩多少天
        int daysRemaining = calculateDaysUntilNextStep(context);
        
        // 国际化消息模板参数
        attributes.put("daysRemaining", daysRemaining);
        attributes.put("reason", stepModel.getConfig().getFirstOrDefault("reason", "inactivity"));
        attributes.put("realmName", realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName());

        if (nextStep != null) {
            attributes.put("nextStepType", nextStep.getProviderId());
        }

        attributes.put("subjectKey", getSubjectKey(context));
        
        return attributes;
    }

    /** 解析下一步骤 {@code after} 延迟并转换为天数；无下一步时返回 0。 */
    private int calculateDaysUntilNextStep(WorkflowExecutionContext context) {
        WorkflowStep nextStep = context.getNextStep();

        if (nextStep == null || nextStep.getAfter() == null) {
            return 0;
        }

        return Math.toIntExact(DurationConverter.parseDuration(nextStep.getAfter()).toDays());
    }

    /** 通知模板占位符解析器，支持 {@code user.*}、{@code realm.*} 及 workflow.daysUntilNextStep。 */
    private class NotificationPropertyResolver implements PropertyResolver {

        private final KeycloakSession session;
        private final WorkflowExecutionContext context;

        /** @param session Keycloak 会话 @param context 当前工作流执行上下文 */
        public NotificationPropertyResolver(KeycloakSession session, WorkflowExecutionContext context) {
            this.session = session;
            this.context = context;
        }

        /** 按属性名解析用户属性、领域信息或剩余天数。 */
        @Override
        public String resolve(String property) {
            if (property.startsWith("user.")) {
                String userId = context.getResourceId();
                RealmModel realm = session.getContext().getRealm();
                UserModel user = session.users().getUserById(realm, userId);

                if (user == null) {
                    return null;
                }

                String attributeKey = property.substring("user.".length());

                return user.getFirstAttribute(attributeKey);
            } else if (property.startsWith("realm.")) {
                RealmModel realm = session.getContext().getRealm();
                String attributeKey = property.substring("realm.".length());

                if (attributeKey.equals("name")) {
                    return realm.getName();
                } else if (attributeKey.equals("displayName")) {
                    return realm.getDisplayName();
                }

                return null;
            } else if ("workflow.daysUntilNextStep".equals(property)) {
                return String.valueOf(calculateDaysUntilNextStep(context));
            }

            return null;
        }
    }
}
