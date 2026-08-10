/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.authentication.authenticators.browser;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.WebAuthnConstants;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.CredentialValidator;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.authentication.requiredactions.WebAuthnRegisterFactory;
import org.keycloak.common.util.Base64Url;
import org.keycloak.common.util.UriUtils;
import org.keycloak.credential.CredentialProvider;
import org.keycloak.credential.WebAuthnCredentialModelInput;
import org.keycloak.credential.WebAuthnCredentialProvider;
import org.keycloak.credential.WebAuthnCredentialProviderFactory;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.model.WebAuthnAuthenticatorsBean;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.WebAuthnPolicy;
import org.keycloak.models.credential.WebAuthnCredentialModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.utils.StringUtil;

import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.util.exception.WebAuthnException;
import org.jboss.logging.Logger;

import static org.keycloak.WebAuthnConstants.AUTH_ERR_DETAIL_LABEL;
import static org.keycloak.WebAuthnConstants.AUTH_ERR_LABEL;
import static org.keycloak.services.messages.Messages.WEBAUTHN_ERROR_API_GET;
import static org.keycloak.services.messages.Messages.WEBAUTHN_ERROR_AUTH_VERIFICATION;
import static org.keycloak.services.messages.Messages.WEBAUTHN_ERROR_DIFFERENT_USER;
import static org.keycloak.services.messages.Messages.WEBAUTHN_ERROR_REGISTRATION;
import static org.keycloak.services.messages.Messages.WEBAUTHN_ERROR_USER_NOT_FOUND;

/**
 * Authenticator for WebAuthn authentication, which will be typically used when WebAuthn is used as second factor.
 */
public class WebAuthnAuthenticator implements Authenticator, CredentialValidator<WebAuthnCredentialProvider> {

    /** 日志记录器。 */
    private static final Logger logger = Logger.getLogger(WebAuthnAuthenticator.class);
    /** 当前 Keycloak 会话。 */
    protected final KeycloakSession session;

    /** @param session 当前 Keycloak 会话 */
    public WebAuthnAuthenticator(KeycloakSession session) {
        this.session = session;
    }

    @Override
    /** 渲染 WebAuthn 登录挑战页；若用户尚未注册凭证则跳过。 */
    public void authenticate(AuthenticationFlowContext context) {
        LoginFormsProvider form = fillContextForm(context);
        if (form != null) {
            context.challenge(form.createLoginWebAuthn());
        }
    }

    /** 填充 WebAuthn 表单属性：挑战值、RP ID、策略选项及已注册认证器列表。 */
    public LoginFormsProvider fillContextForm(AuthenticationFlowContext context) {
        LoginFormsProvider form = context.form();
 
        Challenge challenge = new DefaultChallenge();
        String challengeValue = Base64Url.encode(challenge.getValue());
        context.getAuthenticationSession().setAuthNote(WebAuthnConstants.AUTH_CHALLENGE_NOTE, challengeValue);
        form.setAttribute(WebAuthnConstants.CHALLENGE, challengeValue);

        WebAuthnPolicy policy = getWebAuthnPolicy(context);
        String rpId = getRpID(context);
        form.setAttribute(WebAuthnConstants.RP_ID, rpId);
        form.setAttribute(WebAuthnConstants.CREATE_TIMEOUT, policy.getCreateTimeout());

        UserModel user = context.getUser();
        boolean isUserIdentified = false;
        if (user != null) {
            // 双因素场景：用户已在前置步骤识别
            WebAuthnMetadataService metadataService = getCredentialProvider(context.getSession()).getMetadataService();
            WebAuthnAuthenticatorsBean authenticators = new WebAuthnAuthenticatorsBean(context.getSession(), context.getRealm(), user, getCredentialType(), metadataService);
            if (authenticators.getAuthenticators().isEmpty()) {
                // 用户尚未注册 WebAuthn 凭证，跳过挑战
                return null;
            }
            isUserIdentified = true;
            form.setAttribute(WebAuthnConstants.ALLOWED_AUTHENTICATORS, authenticators);
        } else {
            // 无标识 / 无密码场景
            // NOP
        }
        form.setAttribute(WebAuthnConstants.IS_USER_IDENTIFIED, Boolean.toString(isUserIdentified));

        // 从策略读取 WebAuthn 选项
        String userVerificationRequirement = policy.getUserVerificationRequirement();
        form.setAttribute(WebAuthnConstants.USER_VERIFICATION, userVerificationRequirement);
        form.setAttribute(WebAuthnConstants.SHOULD_DISPLAY_AUTHENTICATORS, shouldDisplayAuthenticators(context));
        form.setAttribute(WebAuthnConstants.MEDIATION, policy.getMediation());
        form.setAttribute(WebAuthnConstants.AUTHENTICATOR_ATTACHMENT, policy.getAuthenticatorAttachment());

        return form;
    }

    /** @return 领域 WebAuthn 双因素策略 */
    protected WebAuthnPolicy getWebAuthnPolicy(AuthenticationFlowContext context) {
        return context.getRealm().getWebAuthnPolicy();
    }

    /** @return 依赖方 ID（RP ID），未配置时使用请求主机名 */
    protected String getRpID(AuthenticationFlowContext context){
        WebAuthnPolicy policy = getWebAuthnPolicy(context);
        String rpId = policy.getRpId();
        if (rpId == null || rpId.isEmpty()) rpId = context.getUriInfo().getBaseUri().getHost();
        return rpId;
    }

    /** @return 凭证类型（双因素 WebAuthn） */
    protected String getCredentialType() {
        return WebAuthnCredentialModel.TYPE_TWOFACTOR;
    }

    /** @return 是否在前端展示已注册认证器列表 */
    protected boolean shouldDisplayAuthenticators(AuthenticationFlowContext context) {
        return context.getUser() != null;
    }

    @Override
    /** 处理 WebAuthn API 返回的凭证数据，校验签名并确定用户身份。 */
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> params = context.getHttpRequest().getDecodedFormParameters();

        context.getEvent().detail(Details.CREDENTIAL_TYPE, getCredentialType());

        // 接收 navigator.credentials.get() 返回的错误
        String errorMsgFromWebAuthnApi = params.getFirst(WebAuthnConstants.ERROR);
        if (StringUtil.isNotBlank(errorMsgFromWebAuthnApi)) {
            setErrorResponse(context, WEBAUTHN_ERROR_API_GET, errorMsgFromWebAuthnApi);
            return;
        }

        if (StringUtil.isBlank(params.getFirst(WebAuthnConstants.CREDENTIAL_ID))
                || StringUtil.isBlank(params.getFirst(WebAuthnConstants.CLIENT_DATA_JSON))
                || StringUtil.isBlank(params.getFirst(WebAuthnConstants.AUTHENTICATOR_DATA))
                || StringUtil.isBlank(params.getFirst(WebAuthnConstants.SIGNATURE))) {
            setErrorResponse(context, WEBAUTHN_ERROR_API_GET, "Missing compulsory webauthn parameters");
            return;
        }

        String baseUrl = UriUtils.getOrigin(context.getUriInfo().getBaseUri());
        String rpId = getRpID(context);

        Origin origin = new Origin(baseUrl);
        final String challengeNote = context.getAuthenticationSession().getAuthNote(WebAuthnConstants.AUTH_CHALLENGE_NOTE);
        if (challengeNote != null) {
            context.getAuthenticationSession().removeAuthNote(WebAuthnConstants.AUTH_CHALLENGE_NOTE);
        }
        Challenge challenge = new DefaultChallenge(challengeNote);
        ServerProperty server = new ServerProperty(origin, rpId, challenge);

        byte[] credentialId = Base64Url.decode(params.getFirst(WebAuthnConstants.CREDENTIAL_ID));
        byte[] clientDataJSON = Base64Url.decode(params.getFirst(WebAuthnConstants.CLIENT_DATA_JSON));
        byte[] authenticatorData = Base64Url.decode(params.getFirst(WebAuthnConstants.AUTHENTICATOR_DATA));
        byte[] signature = Base64Url.decode(params.getFirst(WebAuthnConstants.SIGNATURE));

        final String userHandle = params.getFirst(WebAuthnConstants.USER_HANDLE);
        final String userId;
        // 存在 User Handle 表示使用了支持 Resident Key 的公钥凭证
        if (StringUtil.isBlank(userHandle)) {
            // 未使用 Resident Key，依赖前置步骤已识别的用户
            // so rely on the user set in a previous step (if available)
            if (context.getUser() != null) {
                userId = context.getUser().getId();
            }
            else {
                setErrorResponse(context, WEBAUTHN_ERROR_USER_NOT_FOUND,
                        "Webauthn credential provided doesn't include user id and user id wasn't provided in a previous step");
                return;
            }
        } else {
            // decode using the same charset as it has been encoded (see: WebAuthnRegister.java)
            userId = new String(Base64Url.decode(userHandle), StandardCharsets.UTF_8);
            if (context.getUser() != null) {
                // Resident Key supported public key credential was used,
                // so need to confirm whether the already authenticated user is equals to one authenticated by the webauthn authenticator
                String firstAuthenticatedUserId = context.getUser().getId();
                if (firstAuthenticatedUserId != null && !firstAuthenticatedUserId.equals(userId)) {
                    context.getEvent()
                            .detail(WebAuthnConstants.FIRST_AUTHENTICATED_USER_ID, firstAuthenticatedUserId)
                            .detail(WebAuthnConstants.AUTHENTICATED_USER_ID, userId);
                    setErrorResponse(context, WEBAUTHN_ERROR_DIFFERENT_USER, null);
                    return;
                }
            } else {
                // Resident Key supported public key credential was used,
                // and the user has not yet been identified
                // so rely on the user authenticated by the webauthn authenticator
                // NOP
            }
        }

        boolean isUVFlagChecked = false;
        String userVerificationRequirement = getWebAuthnPolicy(context).getUserVerificationRequirement();
        if (Constants.WEBAUTHN_POLICY_OPTION_REQUIRED.equals(userVerificationRequirement)) isUVFlagChecked = true;

        UserModel user = session.users().getUserById(context.getRealm(), userId);

        if (user == null) {
            context.getEvent()
                    .detail(WebAuthnConstants.AUTHENTICATED_USER_ID, userId);
            setErrorResponse(context, WEBAUTHN_ERROR_USER_NOT_FOUND, null);
            return;
        }

        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                credentialId,
                authenticatorData,
                clientDataJSON,
                signature
                );

        WebAuthnCredentialModelInput.KeycloakWebAuthnAuthenticationParameters authenticationParameters = new WebAuthnCredentialModelInput.KeycloakWebAuthnAuthenticationParameters(
                server,
                isUVFlagChecked
                );

        WebAuthnCredentialModelInput cred = new WebAuthnCredentialModelInput(getCredentialType());

        cred.setAuthenticationRequest(authenticationRequest);
        cred.setAuthenticationParameters(authenticationParameters);

        boolean result = false;
        try {
            result = user.credentialManager().isValid(cred);
        } catch (WebAuthnException wae) {
            setErrorResponse(context, WEBAUTHN_ERROR_AUTH_VERIFICATION, wae.getMessage());
            return;
        }
        String encodedCredentialID = Base64Url.encode(credentialId);
        if (result) {
            String isUVChecked = Boolean.toString(isUVFlagChecked);
            logger.debugv("WebAuthn Authentication successed. isUserVerificationChecked = {0}, PublicKeyCredentialID = {1}", isUVChecked, encodedCredentialID);
            context.setUser(user);
            context.getEvent()
                .detail(WebAuthnConstants.USER_VERIFICATION_CHECKED, isUVChecked)
                .detail(WebAuthnConstants.PUBKEY_CRED_ID_ATTR, encodedCredentialID);
            context.success(getCredentialType());
        } else {
            context.getEvent()
                .detail(WebAuthnConstants.AUTHENTICATED_USER_ID, userId)
                .detail(WebAuthnConstants.PUBKEY_CRED_ID_ATTR, encodedCredentialID);
            setErrorResponse(context, WEBAUTHN_ERROR_USER_NOT_FOUND, null);
            /*
             * Previously, we called context.cancelLogin() here, but this gave potential attackers
             * information about existing user accounts. To avoid that, we just return here.
             */
        }
    }

    @Override
    /** @return 双因素场景下要求前置步骤已识别用户 */
    public boolean requiresUser() {
        return true;
    }

    @Override
    /** @return 用户是否已配置 WebAuthn 双因素凭证 */
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.credentialManager().isConfiguredFor(getCredentialType());
    }

    @Override
    /** 若用户未注册 WebAuthn 凭证，添加注册必需操作。 */
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // ask the user to do required action to register webauthn authenticator
        AuthenticationSessionModel authenticationSession = session.getContext().getAuthenticationSession();
        if (!authenticationSession.getRequiredActions().contains(WebAuthnRegisterFactory.PROVIDER_ID)) {
            authenticationSession.addRequiredAction(WebAuthnRegisterFactory.PROVIDER_ID);
        }
    }

    @Override
    /** @return WebAuthn 注册必需操作工厂列表 */
    public List<RequiredActionFactory> getRequiredActions(KeycloakSession session) {
        return Collections.singletonList((WebAuthnRegisterFactory)session.getKeycloakSessionFactory().getProviderFactory(RequiredActionProvider.class, WebAuthnRegisterFactory.PROVIDER_ID));
    }

    @Override
    /** 关闭认证器（无资源需释放）。 */
    public void close() {
        // NOP
    }

    @Override
    /** @return WebAuthn 凭证 Provider */
    public WebAuthnCredentialProvider getCredentialProvider(KeycloakSession session) {
        return (WebAuthnCredentialProvider)session.getProvider(CredentialProvider.class, WebAuthnCredentialProviderFactory.PROVIDER_ID);
    }

    /** 根据错误类型记录事件并返回对应失败响应。 */
    protected void setErrorResponse(AuthenticationFlowContext context, final String errorCase, final String errorMessage) {
        Response errorResponse = null;
        switch (errorCase) {
        case WEBAUTHN_ERROR_REGISTRATION:
            logger.warn(errorCase);
            context.getEvent()
                .detail(AUTH_ERR_LABEL, errorCase)
                .error(Errors.INVALID_USER_CREDENTIALS);
            errorResponse = createErrorResponse(context, errorCase);
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS, errorResponse);
            break;
        case WEBAUTHN_ERROR_API_GET:
            logger.warnv("error returned from navigator.credentials.get(). {0}", errorMessage);
            context.getEvent()
                .detail(AUTH_ERR_LABEL, errorCase)
                .detail(AUTH_ERR_DETAIL_LABEL, errorMessage)
                .error(Errors.NOT_ALLOWED);
            errorResponse = createErrorResponse(context, errorCase);
            context.failure(AuthenticationFlowError.INVALID_USER, errorResponse);
            break;
        case WEBAUTHN_ERROR_DIFFERENT_USER:
            logger.warn(errorCase);
            context.getEvent()
                .detail(AUTH_ERR_LABEL, errorCase)
                .error(Errors.DIFFERENT_USER_AUTHENTICATED);
            errorResponse = createErrorResponse(context, errorCase);
            context.failure(AuthenticationFlowError.USER_CONFLICT, errorResponse);
            break;
        case WEBAUTHN_ERROR_AUTH_VERIFICATION:
            logger.warnv("WebAuthn API .get() response validation failure. {0}", errorMessage);
            context.getEvent()
                .detail(AUTH_ERR_LABEL, errorCase)
                .detail(AUTH_ERR_DETAIL_LABEL, errorMessage)
                .error(Errors.INVALID_USER_CREDENTIALS);
            errorResponse = createErrorResponse(context, errorCase);
            context.failure(AuthenticationFlowError.INVALID_USER, errorResponse);
            break;
        case WEBAUTHN_ERROR_USER_NOT_FOUND:
            logger.warn(errorCase);
            context.getEvent()
                    .detail(AUTH_ERR_LABEL, errorCase)
                    .error(Errors.USER_NOT_FOUND);
            errorResponse = createErrorResponse(context, errorCase);
            context.failure(AuthenticationFlowError.UNKNOWN_USER, errorResponse);
            break;
        default:
                // NOP
        }
    }

    /** 构建 WebAuthn 错误页，已识别用户时附带可用认证器列表。 */
    protected Response createErrorResponse(AuthenticationFlowContext context, final String errorCase) {
        LoginFormsProvider provider = context.form().setError(errorCase, "");
        UserModel user = context.getUser();
        if (user != null) {
            WebAuthnMetadataService metadataService = getCredentialProvider(context.getSession()).getMetadataService();
            WebAuthnAuthenticatorsBean authenticators = new WebAuthnAuthenticatorsBean(context.getSession(), context.getRealm(), user, getCredentialType(), metadataService);
            if (authenticators.getAuthenticators() != null) {
                provider.setAttribute(WebAuthnConstants.ALLOWED_AUTHENTICATORS, authenticators);
            }
        }
        return provider.createWebAuthnErrorPage();
    }
}
