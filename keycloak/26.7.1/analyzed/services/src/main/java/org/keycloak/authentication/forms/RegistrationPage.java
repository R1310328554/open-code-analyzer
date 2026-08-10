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

package org.keycloak.authentication.forms;

import java.util.List;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.keycloak.Config;
import org.keycloak.authentication.FormAuthenticator;
import org.keycloak.authentication.FormAuthenticatorFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.actiontoken.inviteorg.InviteOrgActionToken;
import org.keycloak.common.VerificationException;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.OrganizationModel;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RegistrationPage implements FormAuthenticator, FormAuthenticatorFactory {

    /** 表单字段：密码确认。 */
    public static final String FIELD_PASSWORD_CONFIRM = "password-confirm";
    /** 表单字段：密码。 */
    public static final String FIELD_PASSWORD = "password";
    /** 表单字段：邮箱。 */
    public static final String FIELD_EMAIL = "email";
    /** 表单字段：用户名。 */
    public static final String FIELD_USERNAME = "username";
    /** 表单字段：姓。 */
    public static final String FIELD_LAST_NAME = "lastName";
    /** 表单字段：名。 */
    public static final String FIELD_FIRST_NAME = "firstName";
    /** 表单字段：reCAPTCHA 响应。 */
    public static final String FIELD_RECAPTCHA_RESPONSE = "g-recaptcha-response";
    /** Provider ID：registration-page-form。 */
    public static final String PROVIDER_ID = "registration-page-form";

    @Override
    /** 渲染注册页；组织邀请场景下预填邮箱并设置组织名称。 */
    public Response render(FormContext context, LoginFormsProvider form) {
        if (Organizations.isEnabled(context.getSession())) {
            try {
                InviteOrgActionToken token = Organizations.parseInvitationToken(context.getSession(), context.getHttpRequest());

                if (token != null) {
                    KeycloakSession session = context.getSession();
                    OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
                    OrganizationModel organization = provider.getById(token.getOrgId());

                    if (organization == null || !organization.isEnabled()) {
                        return form.setError(Messages.EXPIRED_ACTION).createErrorPage(Status.BAD_REQUEST);
                    }

                    form.setAttribute("messageHeader", Messages.REGISTER_ORGANIZATION_MEMBER);
                    form.setAttribute(OrganizationModel.ORGANIZATION_NAME_ATTRIBUTE, organization.getName());
                    form.setAttribute(FIELD_EMAIL, token.getEmail());
                }
            } catch (VerificationException e) {
                return form.setError(Messages.EXPIRED_ACTION).createErrorPage(Status.BAD_REQUEST);
            }
        }

        return form.createRegistration();
    }

    @Override
    public void close() {

    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Registration Page";
    }

    @Override
    /** @return 帮助说明：注册页表单控制器 */
    public String getHelpText() {
        return "This is the controller for the registration page";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public String getReferenceCategory() {
        return null;
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    /** @return 自身作为单例表单认证器 */
    public FormAuthenticator create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    /** @return Provider ID */
    public String getId() {
        return PROVIDER_ID;
    }
}
