package org.keycloak.protocol.saml.mappers;

import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;

/**
 * SAML NameID 映射器接口：根据协议映射器配置生成 Assertion 中的 NameID 值。
 * <p>实现类可基于用户属性、持久化 ID 等策略覆盖默认 NameID 生成逻辑。</p>
 */
public interface SAMLNameIdMapper {

    /**
     * 按 NameID 格式与映射器配置计算 NameID 字符串。
     * @param nameIdFormat 请求的 NameID 格式 URI
     * @param mappingModel 协议映射器配置
     * @param session Keycloak 会话
     * @param userSession 用户会话
     * @param clientSession 已认证客户端会话
     * @return NameID 值，无法映射时可为 null
     */
    String mapperNameId(String nameIdFormat, ProtocolMapperModel mappingModel, KeycloakSession session,
                                        UserSessionModel userSession, AuthenticatedClientSessionModel clientSession);

}