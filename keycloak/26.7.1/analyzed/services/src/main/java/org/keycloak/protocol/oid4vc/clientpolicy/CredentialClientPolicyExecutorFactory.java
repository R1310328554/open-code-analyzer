package org.keycloak.protocol.oid4vc.clientpolicy;

import java.util.Collections;
import java.util.List;

import org.keycloak.Config;
import org.keycloak.common.Profile;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.clientpolicy.executor.ClientPolicyExecutorProviderFactory;

/**
 * {@link CredentialClientPolicyExecutor} 的 SPI 工厂。
 * <p>需同时启用 CLIENT_POLICIES 与 OID4VC_VCI 特性。</p>
 */
public class CredentialClientPolicyExecutorFactory implements ClientPolicyExecutorProviderFactory {

    /** 客户端策略执行器 Provider ID。 */
    public static final String PROVIDER_ID = "oid4vci-policy-executor";

    /** {@inheritDoc} 创建凭证策略执行器实例。 */
    @Override
    public CredentialClientPolicyExecutor create(KeycloakSession session) {
        return new CredentialClientPolicyExecutor(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    /** {@inheritDoc} 执行器说明：校验 Credential Offer 相关客户端策略。 */
    @Override
    public String getHelpText() {
        return "This executor checks client policies related to the credential offer process";
    }

    /** {@inheritDoc} 无额外可配置属性。 */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    /** {@inheritDoc} 需 CLIENT_POLICIES 与 OID4VC_VCI 均已启用。 */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.CLIENT_POLICIES)
                && Profile.isFeatureEnabled(Profile.Feature.OID4VC_VCI);
    }
}
