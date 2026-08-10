package org.keycloak.protocol.oidc.mappers;

import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.AccessTokenResponse;

/**
 * OIDC 访问令牌响应映射器接口：在令牌端点响应中注入自定义声明。
 * <p>实现类可修改 {@link org.keycloak.representations.AccessTokenResponse} 的附加字段。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface OIDCAccessTokenResponseMapper {

    /**
     * 转换访问令牌响应，按映射器配置写入声明。
     * @param accessTokenResponse 当前令牌响应
     * @param mappingModel 协议映射器配置
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSessionCtx 客户端会话上下文
     * @return 更新后的访问令牌响应
     */
    AccessTokenResponse transformAccessTokenResponse(AccessTokenResponse accessTokenResponse, ProtocolMapperModel mappingModel,
                                                     KeycloakSession session, UserSessionModel userSession,
                                                     ClientSessionContext clientSessionCtx);
}
