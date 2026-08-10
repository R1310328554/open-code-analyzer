package org.keycloak.protocol.docker.mapper;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.docker.DockerResponseToken;

/**
 * Docker Auth v2 属性映射器接口：定义对 {@link DockerResponseToken} 的适用性与变换逻辑。
 * <p>由 {@link DockerAuthV2ProtocolMapper} 子类实现，在令牌签发阶段注入访问权限。</p>
 */
public interface DockerAuthV2AttributeMapper {

    /** 判断此映射器是否应处理给定响应令牌。 */
    boolean appliesTo(DockerResponseToken responseToken);

    /**
     * 变换 Docker 响应令牌中的访问属性。
     * @param responseToken 待变换的响应令牌
     * @param mappingModel 协议映射器配置
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSession 客户端会话
     * @return 变换后的响应令牌
     */
    DockerResponseToken transformDockerResponseToken(DockerResponseToken responseToken, ProtocolMapperModel mappingModel,
                                                     KeycloakSession session, UserSessionModel userSession, AuthenticatedClientSessionModel clientSession);
}
