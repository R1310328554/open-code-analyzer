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
import java.util.function.Consumer;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.keycloak.Config;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormActionFactory;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.actiontoken.inviteorg.InviteOrgActionToken;
import org.keycloak.authentication.requiredactions.TermsAndConditions;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.Time;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventType;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.OrganizationInvitationModel;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.organization.InvitationManager;
import org.keycloak.organization.OrganizationProvider;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import org.keycloak.userprofile.Attributes;
import org.keycloak.userprofile.UserProfile;
import org.keycloak.userprofile.UserProfileContext;
import org.keycloak.userprofile.UserProfileProvider;
import org.keycloak.userprofile.ValidationException;

import static org.keycloak.services.managers.AuthenticationManager.NEW_USER_REGISTERED;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class RegistrationUserCreation implements FormAction, FormActionFactory {

    /** Provider ID：registration-user-creation。 */
    public static final String PROVIDER_ID = "registration-user-creation";

    @Override
    public String getHelpText() {
        return "This action must always be first! Validates the username and user profile of the user in validation phase.  In success phase, this will create the user in the database including his user profile.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    /** 校验用户档案字段及组织邀请令牌（若存在）。 */
    public void validate(ValidationContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        context.getEvent().detail(Details.REGISTER_METHOD, "form");

        UserProfile profile = getOrCreateUserProfile(context, formData);
        Attributes attributes = profile.getAttributes();
        String email = attributes.getFirst(UserModel.EMAIL);

        if (!validateOrganizationInvitation(context, formData, email)) {
            return;
        }

        String username = attributes.getFirst(UserModel.USERNAME);
        String firstName = attributes.getFirst(UserModel.FIRST_NAME);
        String lastName = attributes.getFirst(UserModel.LAST_NAME);
        context.getEvent().detail(Details.EMAIL, email);

        context.getEvent().detail(Details.USERNAME, username);
        context.getEvent().detail(Details.FIRST_NAME, firstName);
        context.getEvent().detail(Details.LAST_NAME, lastName);

        if (context.getRealm().isRegistrationEmailAsUsername()) {
            context.getEvent().detail(Details.USERNAME, email);
        }

        try {
            profile.validate();
        } catch (ValidationException pve) {
            List<FormMessage> errors = Validation.getFormErrorsFromValidation(pve.getErrors());

            if (pve.hasError(Messages.EMAIL_EXISTS, Messages.INVALID_EMAIL)) {
                context.getEvent().detail(Details.EMAIL, attributes.getFirst(UserModel.EMAIL));
            }

            if (pve.hasError(Messages.EMAIL_EXISTS)) {
                context.error(Errors.EMAIL_IN_USE);
            } else if (pve.hasError(Messages.USERNAME_EXISTS)) {
                context.error(Errors.USERNAME_IN_USE);
            } else {
                context.error(Errors.INVALID_REGISTRATION);
            }

            context.validationError(formData, errors);
            return;
        }
        context.success();
    }

    @Override
    /** 检查是否已有其他用户正在认证（防止浏览器回退导致异常状态）。 */
    public void buildPage(FormContext context, LoginFormsProvider form) {
        checkNotOtherUserAuthenticating(context);
    }

    @Override
    /** 创建用户、处理条款接受、组织成员关系及登录事件。 */
    public void success(FormContext context) {
        checkNotOtherUserAuthenticating(context);

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String email = formData.getFirst(UserModel.EMAIL);
        String username = formData.getFirst(UserModel.USERNAME);

        if (context.getRealm().isRegistrationEmailAsUsername()) {
            username = email;
        }

        context.getEvent().detail(Details.USERNAME, username)
                .detail(Details.REGISTER_METHOD, "form")
                .detail(Details.EMAIL, email);

        UserProfile profile = getOrCreateUserProfile(context, formData);
        UserModel user = profile.create();
        context.getAuthenticationSession().setAuthNote(NEW_USER_REGISTERED, "true");

        addOrganizationMember(context, user);

        user.setEnabled(true);

        if ("on".equals(formData.getFirst(RegistrationTermsAndConditions.FIELD))) {
            // 若勾选条款复选框，移除 TERMS_AND_CONDITIONS 必需操作并记录接受时间
            RequiredActionProviderModel tacModel = context.getRealm().getRequiredActionProviderByAlias(
                    UserModel.RequiredAction.TERMS_AND_CONDITIONS.name());
            if (tacModel != null && tacModel.isEnabled()) {
                user.setSingleAttribute(TermsAndConditions.USER_ATTRIBUTE, Integer.toString(Time.currentTime()));
                context.getAuthenticationSession().removeRequiredAction(UserModel.RequiredAction.TERMS_AND_CONDITIONS);
                user.removeRequiredAction(UserModel.RequiredAction.TERMS_AND_CONDITIONS);
            }
        }

        context.setUser(user);

        context.getAuthenticationSession().setClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM, username);

        context.getEvent().user(user);
        context.getEvent().success();
        context.newEvent().event(EventType.LOGIN);
        context.getEvent().client(context.getAuthenticationSession().getClient().getClientId())
                .detail(Details.REDIRECT_URI, context.getAuthenticationSession().getRedirectUri())
                .detail(Details.AUTH_METHOD, context.getAuthenticationSession().getProtocol());
        String authType = context.getAuthenticationSession().getAuthNote(Details.AUTH_TYPE);
        if (authType != null) {
            context.getEvent().detail(Details.AUTH_TYPE, authType);
        }
    }

    /** 若上下文中已有用户则抛出认证流程异常（浏览器回退场景）。 */
    private void checkNotOtherUserAuthenticating(FormContext context) {
        if (context.getUser() != null) {
            // 用户可能通过浏览器回退进入异常状态
            context.getEvent().detail(Details.EXISTING_USER, context.getUser().getUsername());
            throw new AuthenticationFlowException(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, Errors.DIFFERENT_USER_AUTHENTICATING, Messages.EXPIRED_ACTION);
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }


    @Override
    public void close() {

    }

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Registration User Profile Creation";
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
    public FormAction create(KeycloakSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    /** @return Provider ID */
    public String getId() {
        return PROVIDER_ID;
    }

    /** 移除 reCAPTCHA 与密码字段，避免污染用户档案数据。 */
    private MultivaluedMap<String, String> normalizeFormParameters(MultivaluedMap<String, String> formParams) {
        MultivaluedHashMap<String, String> copy = new MultivaluedHashMap<>(formParams);

        // 移除 reCAPTCHA 字段以避免长度校验错误
        copy.remove(RegistrationPage.FIELD_RECAPTCHA_RESPONSE);
        // 移除密码字段，防止泄露至用户档案
        copy.remove(RegistrationPage.FIELD_PASSWORD);
        copy.remove(RegistrationPage.FIELD_PASSWORD_CONFIRM);

        return copy;
    }

    /**
     * 获取或创建当前 HTTP 请求的用户档案实例（Keycloak 注册流程中每请求仅一名用户）。
     */
    /** 从会话缓存或表单数据创建 REGISTRATION 上下文用户档案。 */
    public UserProfile getOrCreateUserProfile(FormContext formContext, MultivaluedMap<String, String> formData) {
        KeycloakSession session = formContext.getSession();
        UserProfile profile = (UserProfile) session.getAttribute("UP_REGISTER");
        if (profile == null) {
            formData = normalizeFormParameters(formData);
            UserProfileProvider profileProvider = session.getProvider(UserProfileProvider.class);
            profile = profileProvider.create(UserProfileContext.REGISTRATION, formData);
            session.setAttribute("UP_REGISTER", profile);
        }
        return profile;
    }

    /** 校验组织邀请令牌有效性及邮箱匹配。 */
    private boolean validateOrganizationInvitation(ValidationContext context, MultivaluedMap<String, String> formData, String email) {
        if (Organizations.isEnabled(context.getSession())) {
            Consumer<List<FormMessage>> error = messages -> {
                context.error(Errors.INVALID_TOKEN);
                context.validationError(formData, messages);
            };

            InviteOrgActionToken token;

            try {
                token = Organizations.parseInvitationToken(context.getSession(), context.getHttpRequest());
            } catch (VerificationException e) {
                error.accept(List.of(new FormMessage("Unexpected error parsing the invitation token")));
                return false;
            }

            if (token == null) {
                return true;
            }

            KeycloakSession session = context.getSession();
            OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
            OrganizationModel organization = provider.getById(token.getOrgId());

            if (organization == null) {
                error.accept(List.of(new FormMessage("The provided token contains an invalid organization id")));
                return false;
            }

            if (!organization.isEnabled()) {
                error.accept(List.of(new FormMessage("The organization is not available at this time.")));
                return false;
            }

            // 将会组织写入会话，以便用户档案组织校验器运行
            session.getContext().setOrganization(organization);
            session.setAttribute(InviteOrgActionToken.class.getName(), token);

            if (token.isExpired() || !token.getActionId().equals(InviteOrgActionToken.TOKEN_TYPE)) {
                error.accept(List.of(new FormMessage("The provided token is not valid or has expired.")));
                return false;
            }

            // 校验邀请记录在数据库中仍有效
            InvitationManager invitationManager = provider.getInvitationManager();
            OrganizationInvitationModel invitation = invitationManager.getById(token.getId());

            if (invitation == null || invitation.isExpired()) {
                error.accept(List.of(new FormMessage("The invitation has expired or is no longer valid.")));
                return false;
            }

            if (!token.getEmail().equals(email)) {
                error.accept(List.of(new FormMessage(UserModel.EMAIL, "Email does not match the invitation")));
                return false;
            }
        }

        return true;
    }

    /** 将新用户加入邀请组织并删除已使用的邀请记录。 */
    private void addOrganizationMember(FormContext context, UserModel user) {
        if (Organizations.isEnabled(context.getSession())) {
            InviteOrgActionToken token = (InviteOrgActionToken) context.getSession().getAttribute(InviteOrgActionToken.class.getName());

            if (token != null) {
                KeycloakSession session = context.getSession();
                OrganizationProvider provider = session.getProvider(OrganizationProvider.class);
                OrganizationModel orgModel = provider.getById(token.getOrgId());
                provider.addManagedMember(orgModel, user);
                context.getAuthenticationSession().setRedirectUri(token.getRedirectUri());

                // 邀请已使用，从数据库删除
                InvitationManager invitationManager = provider.getInvitationManager();
                invitationManager.remove(token.getId());

                context.getEvent()
                    .clone()
                    .event(EventType.INVITE_ORG)
                    .user(user)
                    .detail(Details.USERNAME, user.getUsername())
                    .detail(Details.ORG_ID, orgModel.getId())
                    .success();
            }
        }
    }
}
