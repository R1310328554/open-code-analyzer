package org.keycloak.protocol.docker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.util.SecretGenerator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.jose.jws.JWSBuilder;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeyManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.ClientData;
import org.keycloak.protocol.LoginProtocol;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.protocol.docker.mapper.DockerAuthV2AttributeMapper;
import org.keycloak.representations.docker.DockerResponse;
import org.keycloak.representations.docker.DockerResponseToken;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.util.TokenUtil;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.jaxrs.ResponseBuilderImpl;

/**
 * Docker Registry V2 认证协议实现：在用户登录成功后签发 JWT 访问令牌供 Docker 客户端拉取/推送镜像。
 * <p>协议 ID 为 {@link #LOGIN_PROTOCOL}（{@code docker-v2}）；不支持刷新令牌、内省及标准登出流程。</p>
 */
public class DockerAuthV2Protocol implements LoginProtocol {
    protected static final Logger logger = Logger.getLogger(DockerEndpoint.class);

    /** 登录协议标识：{@code docker-v2}。 */
    public static final String LOGIN_PROTOCOL = "docker-v2";
    /** Docker 授权请求参数：账户名（通常由 Basic 认证提供）。 */
    public static final String ACCOUNT_PARAM = "account";
    /** Docker 授权请求参数：服务名，对应 Keycloak 客户端 ID。 */
    public static final String SERVICE_PARAM = "service";
    /** Docker 授权请求参数：请求的仓库/操作范围。 */
    public static final String SCOPE_PARAM = "scope";
    /** 认证会话备注键：JWT issuer，避免与 OIDC 备注冲突。 */
    public static final String ISSUER = "docker.iss"; // don't want to overlap with OIDC notes
    /** JWT 响应中 {@code issued_at} 的 ISO-8601 格式。 */
    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private KeycloakSession session;
    private RealmModel realm;
    private UriInfo uriInfo;
    private HttpHeaders headers;
    private EventBuilder event;

    public DockerAuthV2Protocol() {
    }

    /** 构造带完整上下文的 Docker 协议实例。 */
    public DockerAuthV2Protocol(final KeycloakSession session, final RealmModel realm, final UriInfo uriInfo, final HttpHeaders headers, final EventBuilder event) {
        this.session = session;
        this.realm = realm;
        this.uriInfo = uriInfo;
        this.headers = headers;
        this.event = event;
    }

    @Override
    public LoginProtocol setSession(final KeycloakSession session) {
        this.session = session;
        return this;
    }

    @Override
    public LoginProtocol setRealm(final RealmModel realm) {
        this.realm = realm;
        return this;
    }

    @Override
    public LoginProtocol setUriInfo(final UriInfo uriInfo) {
        this.uriInfo = uriInfo;
        return this;
    }

    @Override
    public LoginProtocol setHttpHeaders(final HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public LoginProtocol setEventBuilder(final EventBuilder event) {
        this.event = event;
        return this;
    }

    @Override
    /**
     * 认证成功回调：构建 {@link DockerResponseToken}，经映射器装饰后 RSA 签名并返回 JSON 响应。
     * @param authSession 认证会话
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @return 含 token/expires_in/issued_at 的 Docker 认证响应
     */
    public Response authenticated(final AuthenticationSessionModel authSession, final UserSessionModel userSession, final ClientSessionContext clientSessionCtx) {
        // 第一步：填充 realm 与用户基本信息构建基础响应令牌
        final AuthenticatedClientSessionModel clientSession = clientSessionCtx.getClientSession();
        final ClientModel client = clientSession.getClient();

        DockerResponseToken responseToken = new DockerResponseToken()
                .id(SecretGenerator.getInstance().generateSecureID())
                .type(TokenUtil.TOKEN_TYPE_BEARER)
                .issuer(authSession.getClientNote(DockerAuthV2Protocol.ISSUER))
                .subject(userSession.getUser().getUsername())
                .issuedNow()
                .audience(client.getClientId())
                .issuedFor(client.getClientId());

        // realm 访问令牌生命周期以秒为单位
        final int accessTokenLifespan = realm.getAccessTokenLifespan();
        responseToken.nbf(responseToken.getIat())
                .exp(responseToken.getIat() + accessTokenLifespan);

        // 第二步：按优先级调用 Docker 协议映射器调整 scope

        AtomicReference<DockerResponseToken> finalResponseToken = new AtomicReference<>(responseToken);
        ProtocolMapperUtils.getSortedProtocolMappers(session, clientSessionCtx, mapper ->
                    mapper.getValue() instanceof DockerAuthV2AttributeMapper && ((DockerAuthV2AttributeMapper) mapper.getValue()).appliesTo(finalResponseToken.get()))
                .forEach(mapper -> finalResponseToken.set(((DockerAuthV2AttributeMapper) mapper.getValue())
                            .transformDockerResponseToken(finalResponseToken.get(), mapper.getKey(), session, userSession, clientSession)));
        responseToken = finalResponseToken.get();

        try {
            // 第三步：RSA 签名 JWT 并组装 Docker 客户端可识别的 JSON 响应
            if (event.getEvent() != null && EventType.LOGIN.equals(event.getEvent().getType())) {
                final KeyManager.ActiveRsaKey activeKey = session.keys().getActiveRsaKey(realm);
                final String encodedToken = new JWSBuilder()
                        .kid(new DockerKeyIdentifier(activeKey.getPublicKey()).toString())
                        .type("JWT")
                        .jsonContent(responseToken)
                        .rsa256(activeKey.getPrivateKey());
                final String expiresInIso8601String = new SimpleDateFormat(ISO_8601_DATE_FORMAT).format(new Date(responseToken.getIat() * 1000L));

                final DockerResponse responseEntity = new DockerResponse()
                        .setToken(encodedToken)
                        .setExpires_in(accessTokenLifespan)
                        .setIssued_at(expiresInIso8601String);
                return new ResponseBuilderImpl().status(Response.Status.OK).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).entity(responseEntity).build();
            } else {
                event.detail(Details.REASON, "Unable to handle request. Currently only LOGIN event types are supported by docker protocol.");
                event.error(Errors.INVALID_REQUEST);
                throw new ErrorResponseException("invalid_request", "Event type not supported", Response.Status.BAD_REQUEST);
            }
        } catch (final InstantiationException e) {
            event.detail(Details.REASON, "Error attempting to create Key ID for Docker JOSE header: " + e.getMessage());
            event.error(Errors.GENERIC_AUTHENTICATION_ERROR);
            throw new ErrorResponseException("token_error", "Unable to construct JOSE header for JWT", Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    /** 认证流程错误响应（Docker 协议返回 500）。 */
    public Response sendError(final AuthenticationSessionModel clientSession, final LoginProtocol.Error error, String errorMessage) {
        return new ResponseBuilderImpl().status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Override
    public ClientData getClientData(AuthenticationSessionModel authSession) {
        return new ClientData();
    }

    @Override
    public Response sendError(ClientModel client, ClientData clientData, Error error) {
        return new ResponseBuilderImpl().status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @Override
    /** Docker 协议不支持 backchannel 登出。 */
    public Response backchannelLogout(final UserSessionModel userSession, final AuthenticatedClientSessionModel clientSession) {
        return errorResponse(userSession, "backchannelLogout");
    }

    @Override
    /** Docker 协议不支持 frontchannel 登出。 */
    public Response frontchannelLogout(final UserSessionModel userSession, final AuthenticatedClientSessionModel clientSession) {
        return errorResponse(userSession, "frontchannelLogout");
    }

    @Override
    /** Docker 协议不支持浏览器登出收尾。 */
    public Response finishBrowserLogout(final UserSessionModel userSession, AuthenticationSessionModel logoutSession) {
        return errorResponse(userSession, "finishLogout");
    }

    @Override
    /** Docker 协议始终要求重新认证。 */
    public boolean requireReauthentication(final UserSessionModel userSession, final AuthenticationSessionModel clientSession) {
        return true;
    }

    /** 记录不支持的方法调用并抛出 {@link ErrorResponseException}。 */
    private Response errorResponse(final UserSessionModel userSession, final String methodName) {
        logger.errorv("User {0} attempted to invoke unsupported method {1} on docker protocol.", userSession.getUser().getUsername(), methodName);
        throw new ErrorResponseException("invalid_request", String.format("Attempted to invoke unsupported docker method %s", methodName), Response.Status.BAD_REQUEST);
    }

    @Override
    public void close() {
        // 无资源需释放
    }
}
