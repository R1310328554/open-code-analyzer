package org.keycloak.protocol.docker;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.keycloak.common.Profile;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.AuthenticationFlowModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.AuthorizationEndpointBase;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequest;
import org.keycloak.protocol.oidc.endpoints.request.AuthorizationEndpointRequestParserProcessor;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.Urls;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.util.CacheControlUtil;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.sessions.CommonClientSessionModel;
import org.keycloak.utils.ProfileHelper;

import org.jboss.logging.Logger;

/**
 * Docker Registry 认证授权端点：解析 account/service/scope 参数并启动浏览器认证流程。
 * <p>继承 {@link AuthorizationEndpointBase}，使用 Realm 配置的 Docker 认证流。</p>
 */
public class DockerEndpoint extends AuthorizationEndpointBase {
    protected static final Logger logger = Logger.getLogger(DockerEndpoint.class);

    private final EventType login;
    private String account;
    private String service;
    private String scope;
    private ClientModel client;
    private AuthenticationSessionModel authenticationSession;

    /** @param session Keycloak 会话 @param event 事件构建器 @param login 登录事件类型 */
    public DockerEndpoint(KeycloakSession session, final EventBuilder event, final EventType login) {
        super(session, event);
        this.login = login;
        event.event(login);
    }

    /** 处理 Docker 客户端 GET 授权请求并返回认证响应或重定向。 */
    @GET
    public Response build() {
        ProfileHelper.requireFeature(Profile.Feature.DOCKER);

        final MultivaluedMap<String, String> params = session.getContext().getUri().getQueryParameters();

        account = params.getFirst(DockerAuthV2Protocol.ACCOUNT_PARAM);
        if (account == null) {
            // account 参数技术上必填，实际用户名由 Basic 认证头提供
        }
        service = params.getFirst(DockerAuthV2Protocol.SERVICE_PARAM);
        scope = params.getFirst(DockerAuthV2Protocol.SCOPE_PARAM);

        checkSsl();
        checkRealm();
        checkService();

        final AuthorizationEndpointRequest authRequest = AuthorizationEndpointRequestParserProcessor.parseRequest(event, session, client, params, AuthorizationEndpointRequestParserProcessor.EndpointType.DOCKER_ENDPOINT);
        authenticationSession = createAuthenticationSession(client, authRequest.getState());

        updateAuthenticationSession();

        // 禁用浏览器后退缓存，避免重复提交
        CacheControlUtil.noBackButtonCacheControlHeader(session);

        return handleBrowserAuthenticationRequest(authenticationSession, new DockerAuthV2Protocol(session, realm, session.getContext().getUri(), headers, event), false, false);
    }

    /** 将会话设为 Docker 协议、Transient 用户会话并写入 Docker 专用 client notes。 */
    private void updateAuthenticationSession() {
        authenticationSession.setProtocol(DockerAuthV2Protocol.LOGIN_PROTOCOL);
        authenticationSession.setAction(CommonClientSessionModel.Action.AUTHENTICATE.name());

        // Docker 协议使用瞬态用户会话：无刷新/内省端点，无需持久化
        authenticationSession.setClientNote(AuthenticationManager.USER_SESSION_PERSISTENT_STATE, UserSessionModel.SessionPersistenceState.TRANSIENT.toString());

        // 写入 Docker 专用认证会话备注（account/service/scope/issuer）
        authenticationSession.setClientNote(DockerAuthV2Protocol.ACCOUNT_PARAM, account);
        authenticationSession.setClientNote(DockerAuthV2Protocol.SERVICE_PARAM, service);
        authenticationSession.setClientNote(DockerAuthV2Protocol.SCOPE_PARAM, scope);
        authenticationSession.setClientNote(DockerAuthV2Protocol.ISSUER, Urls.realmIssuer(session.getContext().getUri().getBaseUri(), realm.getName()));

    }

    /** 校验 service 参数对应客户端存在且已启用。 */
    private void checkService() {
        if (service == null) {
            event.detail(Details.REASON, "Missing parameter: " + DockerAuthV2Protocol.SERVICE_PARAM);
            event.error(Errors.INVALID_REQUEST);
            throw new ErrorResponseException("invalid_request", "service parameter must be provided", Response.Status.BAD_REQUEST);
        }
        event.client(service);
        client = realm.getClientByClientId(service);
        if (client == null) {
            event.detail(Details.REASON, "Client specified by 'service' parameter does not exist");
            event.error(Errors.CLIENT_NOT_FOUND);
            throw new ErrorResponseException("invalid_client", "Client specified by 'service' parameter does not exist", Response.Status.BAD_REQUEST);
        }
        if (!client.isEnabled()) {
            event.detail(Details.REASON, "Client specified by 'service' is disabled");
            event.error(Errors.CLIENT_DISABLED);
            throw new ErrorResponseException("invalid_client", "Client specified by 'service' is disabled", Response.Status.BAD_REQUEST);
        }
        session.getContext().setClient(client);
    }

    @Override
    /** @return Realm 配置的 Docker 认证流 */
    protected AuthenticationFlowModel getAuthenticationFlow(AuthenticationSessionModel authSession) {
        return realm.getDockerAuthenticationFlow();
    }

}
