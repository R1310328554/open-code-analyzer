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

package org.keycloak.protocol.oidc.endpoints;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.OAuth2Constants;
import org.keycloak.authentication.AuthenticationProcessor;
import org.keycloak.common.Profile;
import org.keycloak.constants.AdapterConstants;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.locale.LocaleSelectorProvider;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.Constants;
import org.keycloak.models.KeycloakSession;
import org.keycloak.organization.utils.Organizations;
import org.keycloak.protocol.AuthorizationEndpointBase;
import org.keycloak.protocol.oidc.OIDCAdvancedConfigWrapper;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequestParserProcessor;
import org.keycloak.protocol.oidc.endpoints.request.RequestUriType;
import org.keycloak.protocol.oidc.grants.device.endpoints.DeviceEndpoint;
import org.keycloak.protocol.oidc.utils.AcrUtils;
import org.keycloak.protocol.oidc.utils.OIDCRedirectUriBuilder;
import org.keycloak.protocol.oidc.utils.OIDCResponseMode;
import org.keycloak.protocol.oidc.utils.OIDCResponseType;
import org.keycloak.services.ErrorPageException;
import org.keycloak.services.Urls;
import org.keycloak.services.clientpolicy.ClientPolicyException;
import org.keycloak.services.clientpolicy.context.AuthorizationRequestContext;
import org.keycloak.services.clientpolicy.context.PreAuthorizationRequestContext;
import org.keycloak.services.managers.AuthenticationSessionManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.services.util.CacheControlUtil;
import org.keycloak.services.util.LocaleUtil;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;

import static org.keycloak.protocol.oidc.par.endpoints.ParEndpoint.PAR_DPOP_PROOF_JKT;

/**
 * OIDC 授权端点：解析授权请求、校验参数、创建认证会话并进入浏览器登录流。
 * <p>支持授权码、隐式片段、注册、忘记凭据及 OAuth 2.0 设备授权子路径。</p>
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class AuthorizationEndpoint extends AuthorizationEndpointBase {

    private static final Logger logger = Logger.getLogger(AuthorizationEndpoint.class);

    /** 认证类型 note：授权码流。 */
    public static final String CODE_AUTH_TYPE = "code";

    /**
     * 将客户端附加 GET 参数写入认证会话 note 的前缀，供 Authenticator/RequiredAction 读取。
     * @see AuthenticationSessionModel#getClientNote(String)
     */
    public static final String LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX = "client_request_param_";

    /** 授权端点内部动作：注册、授权码、忘记凭据。 */
    private enum Action {
        REGISTER, CODE, FORGOT_CREDENTIALS
    }

    private ClientModel client;
    private AuthenticationSessionModel authenticationSession;

    private Action action;
    private OIDCResponseType parsedResponseType;
    private OIDCResponseMode parsedResponseMode;

    private AuthorizationEndpointRequest request;
    private String redirectUri;

    /** @param session Keycloak 会话
     * @param event 事件构建器（初始为 LOGIN） */
    public AuthorizationEndpoint(KeycloakSession session, EventBuilder event) {
        super(session, event);
        event.event(EventType.LOGIN);
    }

    /** 处理 form_post 授权请求。 */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response buildPost() {
        logger.trace("Processing @POST request");
        return process(httpRequest.getDecodedFormParameters());
    }

    /** 处理 GET 查询串授权请求。 */
    @GET
    public Response buildGet() {
        logger.trace("Processing @GET request");
        return process(session.getContext().getUri().getQueryParameters());
    }

    /** OAuth 2.0 设备授权端点子资源。 */
    @Path("device")
    public Object authorizeDevice() {
        if (!Profile.isFeatureEnabled(Profile.Feature.DEVICE_FLOW)) {
            return null;
        }
        return new DeviceEndpoint(session, event);
    }

    /** 授权请求主流程：校验、创建认证会话、分发注册/登录/忘记凭据。 */
    private Response process(final MultivaluedMap<String, String> params) {
        String clientId = AuthorizationEndpointRequestParserProcessor.getClientId(event, session, params);

        checkSsl();
        checkRealm();

        try {
            session.clientPolicy().triggerOnEvent(new PreAuthorizationRequestContext(clientId, params));
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            throw new ErrorPageException(session, authenticationSession, cpe.getErrorStatus(), cpe.getErrorDetail());
        }
        checkClient(clientId);

        request = AuthorizationEndpointRequestParserProcessor.parseRequest(event, session, client, params, AuthorizationEndpointRequestParserProcessor.EndpointType.OIDC_AUTH_ENDPOINT);

        AuthorizationEndpointChecker checker = new AuthorizationEndpointChecker()
                .event(event)
                .client(client)
                .realm(realm)
                .request(request)
                .session(session)
                .params(params);

        try {
            checker.checkRedirectUri();
            this.redirectUri = checker.getRedirectUri();
        } catch (AuthorizationEndpointChecker.AuthorizationCheckException ex) {
            checker.throwAsErrorPageException(authenticationSession, ex);
        }

        try {
            checker.checkResponseType();
            this.parsedResponseType = checker.getParsedResponseType();
            this.parsedResponseMode = checker.getParsedResponseMode();
        } catch (AuthorizationEndpointChecker.AuthorizationCheckException ex) {
            OIDCResponseMode responseMode;
            if (checker.isInvalidResponseType(ex)) {
                responseMode = OIDCResponseMode.parseWhenInvalidResponseType(request.getResponseMode());
            } else {
                responseMode = checker.getParsedResponseMode() != null ? checker.getParsedResponseMode() : OIDCResponseMode.QUERY;
            }
            return redirectErrorToClient(responseMode, ex.getError(), ex.getErrorDescription());
        }
        if (action == null) {
            action = AuthorizationEndpoint.Action.CODE;
        }

        try {
            checker.checkParRequired();
            checker.checkInvalidRequestMessage();
            checker.checkAuthorizationDetails();
            checker.checkOIDCRequest();
            checker.checkValidScope();
            checker.checkValidResource();
            checker.checkOIDCParams();
            checker.checkPKCEParams();
            checker.checkProviderAddOns();
        } catch (AuthorizationEndpointChecker.AuthorizationCheckException ex) {
            return redirectErrorToClient(parsedResponseMode, ex.getError(), ex.getErrorDescription());
        }

        // PAR 请求中的 DPoP jkt 需与后续令牌请求一致
        String dpopJkt = session.getAttribute(PAR_DPOP_PROOF_JKT, String.class);
        if (dpopJkt != null) {
            // if dpop_jkt is specified in an authorization request sent to Authorization Endpoint, it is overwritten by one in PAR request
            request.setDpopJkt(dpopJkt);
        }

        authenticationSession = createAuthenticationSession(client, request.getState());

        try {
            session.clientPolicy().triggerOnEvent(new AuthorizationRequestContext(parsedResponseType, request, redirectUri, params, authenticationSession));
        } catch (ClientPolicyException cpe) {
            event.detail(Details.REASON, Details.CLIENT_POLICY_ERROR);
            event.detail(Details.CLIENT_POLICY_ERROR, cpe.getError());
            event.detail(Details.CLIENT_POLICY_ERROR_DETAIL, cpe.getErrorDetail());
            event.error(cpe.getError());
            new AuthenticationSessionManager(session).removeAuthenticationSession(realm, authenticationSession, false);
            return redirectErrorToClient(parsedResponseMode, cpe.getError(), cpe.getErrorDetail());
        }

        updateAuthenticationSession();

        // 禁止浏览器后退缓存
        CacheControlUtil.noBackButtonCacheControlHeader(session);

        // prompt=create 触发用户注册流
        // see: https://openid.net/specs/openid-connect-prompt-create-1_0.html#section-4.1
        String promptValue = Optional
                .ofNullable(params.getFirst(OAuth2Constants.PROMPT))
                .orElseGet(() -> session.getContext().getAuthenticationSession().getClientNote(OAuth2Constants.PROMPT));

        if (OIDCLoginProtocol.PROMPT_VALUE_CREATE.equals(promptValue)) {
            if (!Organizations.isRegistrationAllowed(session, realm)) {
                throw new ErrorPageException(session, authenticationSession, Response.Status.BAD_REQUEST, Messages.REGISTRATION_NOT_ALLOWED);
            }
            return buildRegister();
        }

        switch (action) {
            case REGISTER:
                return buildRegister();
            case FORGOT_CREDENTIALS:
                return buildForgotCredential();
            case CODE:
                return buildAuthorizationCodeAuthorizationResponse(params.getFirst(OIDCLoginProtocol.REQUEST_URI_PARAM));
        }

        throw new RuntimeException("Unknown action " + action);
    }

    /** 配置为注册动作；可选解析组织邀请 token。 */
    public AuthorizationEndpoint register(String tokenString) {
        event.event(EventType.REGISTER);
        action = Action.REGISTER;

        if (Profile.isFeatureEnabled(Profile.Feature.ORGANIZATION) && tokenString != null) {
            // 从 action token 提取 orgId 并写入会话上下文
            Response errorResponse = new LoginActionsService(session, event).preHandleActionToken(tokenString);
            if (errorResponse != null) {
                throw new ErrorPageException(errorResponse);
            }
        }

        if (!Organizations.isRegistrationAllowed(session, realm)) {
            throw new ErrorPageException(session, authenticationSession, Response.Status.BAD_REQUEST, Messages.REGISTRATION_NOT_ALLOWED);
        }

        return this;
    }

    /** 配置为忘记凭据/重置密码动作。 */
    public AuthorizationEndpoint forgotCredentials() {
        event.event(EventType.RESET_PASSWORD);
        action = Action.FORGOT_CREDENTIALS;

        if (!realm.isResetPasswordAllowed()) {
            throw new ErrorPageException(session, authenticationSession, Response.Status.BAD_REQUEST, Messages.RESET_CREDENTIAL_NOT_ALLOWED);
        }

        return this;
    }

    /** 校验 client_id 存在、启用、非 bearer-only 且协议为 openid-connect。 */
    private void checkClient(String clientId) {
        if (clientId == null) {
            event.detail(Details.REASON, "Missing parameter: " + OIDCLoginProtocol.CLIENT_ID_PARAM);
            event.error(Errors.INVALID_REQUEST);
            throw new ErrorPageException(session, authenticationSession, Response.Status.BAD_REQUEST, Messages.MISSING_PARAMETER, OIDCLoginProtocol.CLIENT_ID_PARAM);
        }

        event.client(clientId);

        client = realm.getClientByClientId(clientId);
        if (client == null) {
            event.error(Errors.CLIENT_NOT_FOUND);
            throw new ErrorPageException(session, authenticationSession, Response.Status.BAD_REQUEST, Messages.CLIENT_NOT_FOUND);
        }

        if (!client.isEnabled()) {
            event.error(Errors.CLIENT_DISABLED);
            throw new ErrorPageException(session, authenticationSession, Response.Status.BAD_REQUEST, Messages.CLIENT_DISABLED);
        }

        if (client.isBearerOnly()) {
            event.error(Errors.NOT_ALLOWED);
            throw new ErrorPageException(session, authenticationSession, Response.Status.FORBIDDEN, Messages.BEARER_ONLY);
        }

        String protocol = client.getProtocol();
        if (protocol == null) {
            logger.warnf("Client '%s' doesn't have protocol set. Fallback to openid-connect. Please fix client configuration", clientId);
            protocol = OIDCLoginProtocol.LOGIN_PROTOCOL;
        }
        if (!protocol.equals(OIDCLoginProtocol.LOGIN_PROTOCOL)) {
            String errorMessage = "Wrong client protocol.";
            event.detail(Details.REASON, errorMessage);
            event.error(Errors.INVALID_CLIENT);
            throw new ErrorPageException(session, authenticationSession, Response.Status.BAD_REQUEST, errorMessage);
        }

        session.getContext().setClient(client);
    }

    /** 按 response_mode 将 OAuth 错误重定向回客户端。 */
    private Response redirectErrorToClient(OIDCResponseMode responseMode, String error, String errorDescription) {
        CacheControlUtil.noBackButtonCacheControlHeader(session);

        OIDCRedirectUriBuilder errorResponseBuilder = OIDCRedirectUriBuilder.fromUri(redirectUri, responseMode, session, null)
                .addParam(OAuth2Constants.ERROR, error);

        if (errorDescription != null) {
            errorResponseBuilder.addParam(OAuth2Constants.ERROR_DESCRIPTION, errorDescription);
        }

        if (request.getState() != null) {
            errorResponseBuilder.addParam(OAuth2Constants.STATE, request.getState());
        }

        OIDCAdvancedConfigWrapper clientConfig = OIDCAdvancedConfigWrapper.fromClientModel(client);
        if (!clientConfig.isExcludeIssuerFromAuthResponse()) {
            errorResponseBuilder.addParam(OAuth2Constants.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));
        }

        return errorResponseBuilder.build();
    }

    /** 将授权请求参数、ACR/LoA、附加参数写入认证会话 note。 */
    private void updateAuthenticationSession() {
        authenticationSession.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        authenticationSession.setRedirectUri(redirectUri);
        authenticationSession.setAction(AuthenticationSessionModel.Action.AUTHENTICATE.name());
        authenticationSession.setClientNote(OIDCLoginProtocol.RESPONSE_TYPE_PARAM, request.getResponseType());
        authenticationSession.setClientNote(OIDCLoginProtocol.REDIRECT_URI_PARAM, request.getRedirectUri());
        authenticationSession.setClientNote(OIDCLoginProtocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));

        performActionOnParameters(request, (paramName, paramValue) -> {if (paramValue != null) authenticationSession.setClientNote(paramName, paramValue);});
        if (request.getMaxAge() != null) authenticationSession.setClientNote(OIDCLoginProtocol.MAX_AGE_PARAM, String.valueOf(request.getMaxAge()));
        if (request.getUiLocales() != null) authenticationSession.setClientNote(LocaleSelectorProvider.CLIENT_REQUEST_LOCALE, request.getUiLocales());

        Map<String, Integer> acrLoaMap = AcrUtils.getAcrLoaMap(authenticationSession.getClient());
        List<String> acrValues = AcrUtils.getRequiredAcrValues(request.getClaims());

        if (acrValues.isEmpty()) {
            acrValues = AcrUtils.getAcrValues(request.getClaims(), request.getAcr(), authenticationSession.getClient());
        } else {
            List<String> minimizedAcrValues = AcrUtils.enforceMinimumAcr(acrValues, client);
            // If enforcing a minimum here changes the list, the client has an essential claim that is too low
            if (!minimizedAcrValues.equals(acrValues)) {
                logger.errorf("Requested essential acr value list contains values lower than the client minimum. Please doublecheck the client configuration or correct ACR passed in the 'claims' parameter.");
                event.detail(Details.REASON, "Invalid requested essential acr value");
                event.error(Errors.INVALID_REQUEST);
                throw new ErrorPageException(session, authenticationSession, Response.Status.BAD_REQUEST, Messages.INVALID_PARAMETER, OIDCLoginProtocol.CLAIMS_PARAM);
            }
            authenticationSession.setClientNote(Constants.FORCE_LEVEL_OF_AUTHENTICATION, "true");
        }

        acrValues.stream().mapToInt(acr -> {
            try {
                Integer loa = acrLoaMap.get(acr);
                return loa == null ? Integer.parseInt(acr) : loa;
            } catch (NumberFormatException e) {
                // this is an unknown acr. In case of an essential claim, we directly reject authentication as we cannot met the specification requirement. Otherwise fallback to minimum LoA
                boolean essential = Boolean.parseBoolean(authenticationSession.getClientNote(Constants.FORCE_LEVEL_OF_AUTHENTICATION));
                if (essential) {
                    logger.errorf("Requested essential acr value '%s' is not a number and it is not mapped in the ACR-To-Loa mappings of realm or client. Please doublecheck ACR-to-LOA mapping or correct ACR passed in the 'claims' parameter.", acr);
                    event.detail(Details.REASON, "Invalid requested essential acr value");
                    event.error(Errors.INVALID_REQUEST);
                    throw new ErrorPageException(session, authenticationSession, Response.Status.BAD_REQUEST, Messages.INVALID_PARAMETER, OIDCLoginProtocol.CLAIMS_PARAM);
                } else {
                    logger.warnf("Requested acr value '%s' is not a number and it is not mapped in the ACR-To-Loa mappings of realm or client. Please doublecheck ACR-to-LOA mapping or correct used ACR.", acr);
                    return Constants.MINIMUM_LOA;
                }
            }
        }).min().ifPresent(loa -> authenticationSession.setClientNote(Constants.REQUESTED_LEVEL_OF_AUTHENTICATION, String.valueOf(loa)));


        if (request.getAdditionalReqParams() != null) {
            for (String paramName : request.getAdditionalReqParams().keySet()) {
                authenticationSession.setClientNote(LOGIN_SESSION_NOTE_ADDITIONAL_REQ_PARAMS_PREFIX + paramName, request.getAdditionalReqParams().get(paramName));
            }

            // 保存 authorization_details 供后续 grant 处理
            String authorizationDetails = request.getAdditionalReqParams().get(OAuth2Constants.AUTHORIZATION_DETAILS);
            if (authorizationDetails != null) {
                authenticationSession.setClientNote(OAuth2Constants.AUTHORIZATION_DETAILS, authorizationDetails);
            }
        }
    }

    /** 进入浏览器认证流（授权码/PAR 等）。 */
    private Response buildAuthorizationCodeAuthorizationResponse(String requestUri) {
        this.event.event(EventType.LOGIN);
        authenticationSession.setAuthNote(Details.AUTH_TYPE, CODE_AUTH_TYPE);
        authenticationSession.setAuthNote(Constants.AUTHORIZATION_REQUEST_URI, requestUri);

        RequestUriType requestUriType = Optional.ofNullable(requestUri)
                .map(AuthorizationEndpointRequestParserProcessor::getRequestUriType)
                .orElse(null);

        // PAR 请求需重定向到认证流（单次 request_uri 已消费）
        boolean redirectToAuthFlow = requestUriType == RequestUriType.PAR;

        OIDCLoginProtocol loginProtocol = new OIDCLoginProtocol(session, realm, session.getContext().getUri(), headers, event);
        boolean isPassive = TokenUtil.hasPrompt(request.getPrompt(), OIDCLoginProtocol.PROMPT_VALUE_NONE);
        Response response = handleBrowserAuthenticationRequest(authenticationSession, loginProtocol, isPassive, redirectToAuthFlow);
        return response;
    }

    /** 启动 Realm 注册认证流。 */
    private Response buildRegister() {
        authManager.expireIdentityCookie(session);

        AuthenticationFlowModel flow = realm.getRegistrationFlow();
        String flowId = flow.getId();

        AuthenticationProcessor processor = createProcessor(authenticationSession, flowId, LoginActionsService.REGISTRATION_PATH);
        authenticationSession.setClientNote(APP_INITIATED_FLOW, LoginActionsService.REGISTRATION_PATH);
        LocaleUtil.processLocaleParam(session, realm, authenticationSession);

        return processor.authenticate();
    }

    /** 启动重置凭据认证流。 */
    private Response buildForgotCredential() {
        authManager.expireIdentityCookie(session);

        AuthenticationFlowModel flow = realm.getResetCredentialsFlow();
        String flowId = flow.getId();

        AuthenticationProcessor processor = createProcessor(authenticationSession, flowId, LoginActionsService.RESET_CREDENTIALS_PATH);
        authenticationSession.setClientNote(APP_INITIATED_FLOW, LoginActionsService.RESET_CREDENTIALS_PATH);

        return processor.authenticate();
    }

    /** 将标准 OIDC 授权参数逐项交给回调处理（写入 session note 等）。 */
    public static void performActionOnParameters(AuthorizationEndpointRequest request, BiConsumer<String, String> paramAction) {
        paramAction.accept(AdapterConstants.KC_IDP_HINT, request.getIdpHint());

        String kcAction = request.getAction();
        String kcActionParameter = null;
        if (kcAction != null && kcAction.contains(":")) {
            String[] splits = kcAction.split(":");
            kcAction = splits[0];
            kcActionParameter = splits[1];
        }
        paramAction.accept(Constants.KC_ACTION, kcAction);
        paramAction.accept(Constants.KC_ACTION_PARAMETER, kcActionParameter);

        paramAction.accept(OAuth2Constants.DISPLAY, request.getDisplay());
        paramAction.accept(OIDCLoginProtocol.ACR_PARAM, request.getAcr());
        paramAction.accept(OIDCLoginProtocol.CLAIMS_PARAM, request.getClaims());
        paramAction.accept(OIDCLoginProtocol.CODE_CHALLENGE_METHOD_PARAM, request.getCodeChallengeMethod());
        paramAction.accept(OIDCLoginProtocol.CODE_CHALLENGE_PARAM, request.getCodeChallenge());
        paramAction.accept(OIDCLoginProtocol.LOGIN_HINT_PARAM, request.getLoginHint());
        paramAction.accept(OIDCLoginProtocol.NONCE_PARAM, request.getNonce());
        paramAction.accept(OIDCLoginProtocol.PROMPT_PARAM, request.getPrompt());
        paramAction.accept(OIDCLoginProtocol.RESPONSE_MODE_PARAM, request.getResponseMode());
        paramAction.accept(OIDCLoginProtocol.SCOPE_PARAM, request.getScope());
        paramAction.accept(OIDCLoginProtocol.RESOURCE_PARAM, request.getResource());
        paramAction.accept(OIDCLoginProtocol.STATE_PARAM, request.getState());
        paramAction.accept(OIDCLoginProtocol.DPOP_JKT, request.getDpopJkt());
    }
}
