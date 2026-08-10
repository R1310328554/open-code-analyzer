package org.keycloak.testsuite.authentication;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.protocol.saml.ArtifactResolver;
import org.keycloak.protocol.saml.ArtifactResolverFactory;
import org.keycloak.protocol.saml.util.ArtifactBindingUtils;

/**
 * 仅用于测试的 SAML Artifact 解析器工厂，注册自定义类型码与端点索引。
 */
public class CustomTestingSamlArtifactResolverFactory implements ArtifactResolverFactory {

    /** 类型码与端点索引（各 2 字节），构建 artifact 时必须存在。 */
    public  static final byte[] TYPE_CODE_AND_INDEX = {0, 5, 0, 0}; // type code and endpoint index must be present, 2 bytes each
    /** 单例解析器实例，供所有会话复用。 */
    public static final CustomTestingSamlArtifactResolver resolver = new CustomTestingSamlArtifactResolver();
    
    /** {@inheritDoc} 返回共享的测试解析器实例。 */
    @Override
    public ArtifactResolver create(KeycloakSession session) {
        return resolver;
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

    /** {@inheritDoc} 根据 {@link #TYPE_CODE_AND_INDEX} 生成提供者标识符。 */
    @Override
    public String getId() {
        return ArtifactBindingUtils.byteArrayToResolverProviderId(TYPE_CODE_AND_INDEX);
    }
}
