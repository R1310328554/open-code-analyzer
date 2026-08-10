package org.keycloak.protocol.oidc.mappers;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.AccessToken;

/**
 * 令牌内省映射器接口：在内省端点响应中注入自定义声明。
 * <p>内省响应以 {@link org.keycloak.representations.AccessToken} 结构返回。</p>
 */
public interface TokenIntrospectionTokenMapper {
    /**
     * 转换内省令牌响应，按映射器配置写入声明。
     * @param token 当前内省令牌表示
     * @param mappingModel 协议映射器配置
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @return 更新后的内省令牌
     */
    AccessToken transformIntrospectionToken(AccessToken token, ProtocolMapperModel mappingModel, KeycloakSession session,
                                       UserSessionModel userSession, ClientSessionContext clientSessionCtx);
}
