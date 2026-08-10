package org.keycloak.protocol.docker.mapper;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.docker.DockerAuthV2Protocol;
import org.keycloak.representations.docker.DockerAccess;
import org.keycloak.representations.docker.DockerResponseToken;

/**
 * Docker Auth v2 “Allow All” 协议映射器：按请求 scope 填充令牌访问项。
 * <p>若响应令牌中已有超出请求范围的 scope，将被清除后仅保留客户端请求的 scope。</p>
 */
public class AllowAllDockerProtocolMapper extends DockerAuthV2ProtocolMapper implements DockerAuthV2AttributeMapper {

    /** 协议映射器提供方 ID。 */
    public static final String PROVIDER_ID = "docker-v2-allow-all-mapper";

    @Override
    /** @return 管理控制台显示名称 */
    public String getDisplayType() {
        return "Allow All";
    }

    @Override
    /** @return 映射器帮助说明文本 */
    public String getHelpText() {
        return "Allows all grants, returning the full set of requested access attributes as permitted attributes.";
    }

    @Override
    /** @return 提供方 ID */
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    /** 对所有 Docker 响应令牌均适用。 */
    public boolean appliesTo(final DockerResponseToken responseToken) {
        return true;
    }

    @Override
    /**
     * 清空现有访问项，按客户端会话中记录的请求 scope 重建 {@link DockerAccess} 列表。
     * @param responseToken Docker 响应令牌
     * @param mappingModel 协议映射器模型
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSession 客户端会话（含 {@code scope} note）
     * @return 变换后的响应令牌
     */
    public DockerResponseToken transformDockerResponseToken(final DockerResponseToken responseToken, final ProtocolMapperModel mappingModel,
                                                            final KeycloakSession session, final UserSessionModel userSession, final AuthenticatedClientSessionModel clientSession) {

        responseToken.getAccessItems().clear();

        final String requestedScopes = clientSession.getNote(DockerAuthV2Protocol.SCOPE_PARAM);
        if (requestedScopes != null) {
            for (String requestedScope : requestedScopes.split(" ")) {
                final DockerAccess requestedAccess = new DockerAccess(requestedScope);
                responseToken.getAccessItems().add(requestedAccess);
            }
        }

        return responseToken;
    }
}
