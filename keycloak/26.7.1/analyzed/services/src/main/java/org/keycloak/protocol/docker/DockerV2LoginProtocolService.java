package org.keycloak.protocol.docker;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.keycloak.common.Profile;
import org.keycloak.events.EventBuilder;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.RealmsResource;
import org.keycloak.utils.ProfileHelper;

/**
 * Docker V2 协议 JAX-RS 服务根：暴露 {@code /realms/{realm}/protocol/docker-v2/auth} 授权端点。
 * <p>由 {@link DockerAuthV2ProtocolFactory#createProtocolEndpoint} 注册为协议子资源。</p>
 */
public class DockerV2LoginProtocolService {

    private final EventBuilder event;

    private final KeycloakSession session;

    /** @param session Keycloak 会话 @param event 事件构建器 */
    public DockerV2LoginProtocolService(final KeycloakSession session, final EventBuilder event) {
        this.session = session;
        this.event = event;
    }

    /** 基于请求 URI 构建 Docker 协议基础路径 UriBuilder。 */
    public static UriBuilder authProtocolBaseUrl(final UriInfo uriInfo) {
        final UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return authProtocolBaseUrl(baseUriBuilder);
    }

    /** 构建 {@code /realms/{realm}/protocol/docker-v2} 路径。 */
    public static UriBuilder authProtocolBaseUrl(final UriBuilder baseUriBuilder) {
        return baseUriBuilder.path(RealmsResource.class).path("{realm}/protocol/" + DockerAuthV2Protocol.LOGIN_PROTOCOL);
    }

    /** 基于请求 URI 构建完整授权 URL。 */
    public static UriBuilder authUrl(final UriInfo uriInfo) {
        final UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return authUrl(baseUriBuilder);
    }

    /** 在协议基础路径上追加 {@code /auth} 段。 */
    public static UriBuilder authUrl(final UriBuilder baseUriBuilder) {
        final UriBuilder uriBuilder = authProtocolBaseUrl(baseUriBuilder);
        return uriBuilder.path(DockerV2LoginProtocolService.class, "auth");
    }

    /**
     * Docker 授权端点：委托 {@link DockerEndpoint} 处理登录请求。
     */
    /** @return 新建 {@link DockerEndpoint} 处理 LOGIN 事件 */
    @Path("auth")
    public Object auth() {
        ProfileHelper.requireFeature(Profile.Feature.DOCKER);

        return new DockerEndpoint(session, event, EventType.LOGIN);
    }
}
