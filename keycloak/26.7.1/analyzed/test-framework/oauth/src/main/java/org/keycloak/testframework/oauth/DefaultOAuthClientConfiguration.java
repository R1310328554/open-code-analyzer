package org.keycloak.testframework.oauth;

import java.util.HashMap;
import java.util.Map;

import org.keycloak.protocol.oidc.OIDCConfigAttributes;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.protocol.oidc.mappers.AudienceProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.testframework.realm.ClientBuilder;
import org.keycloak.testframework.realm.ClientConfig;

/**
 * OAuth 集成测试的默认客户端配置。
 * <p>
 * 创建 {@code test-app} 机密客户端，启用服务账户、直接访问授权与 JWT 授权授予，
 * 并添加 audience 协议映射器。
 */
public class DefaultOAuthClientConfiguration implements ClientConfig {

    /** {@inheritDoc} 应用默认 OAuth 测试客户端属性与 audience 映射器。 */
    @Override
    public ClientBuilder configure(ClientBuilder client) {
        ProtocolMapperRepresentation audienceMapper = new ProtocolMapperRepresentation();
        audienceMapper.setName("audience-test-app");
        audienceMapper.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);
        audienceMapper.setProtocolMapper(AudienceProtocolMapper.PROVIDER_ID);

        Map<String, String> audienceConfig = new HashMap<>();
        audienceConfig.put(AudienceProtocolMapper.INCLUDED_CUSTOM_AUDIENCE, "test-app");
        audienceConfig.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
        audienceMapper.setConfig(audienceConfig);

        return client.clientId("test-app")
                .serviceAccountsEnabled(true)
                .directAccessGrantsEnabled(true)
                .attribute(OIDCConfigAttributes.JWT_AUTHORIZATION_GRANT_ENABLED, "true")
                .attribute(OIDCConfigAttributes.JWT_AUTHORIZATION_GRANT_IDP, "authorization-grant-idp-alias")
                .secret("test-secret")
                .protocolMappers(audienceMapper);
    }

}
