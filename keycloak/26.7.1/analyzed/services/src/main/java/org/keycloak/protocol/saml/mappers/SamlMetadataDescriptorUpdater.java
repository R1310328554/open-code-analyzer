package org.keycloak.protocol.saml.mappers;

import org.keycloak.dom.saml.v2.metadata.EntityDescriptorType;
import org.keycloak.models.IdentityProviderMapperModel;

/**
 * SAML 元数据描述符更新器接口：在 IdP 元数据生成阶段注入或修改 EntityDescriptor。
 * <p>通常由身份提供方映射器实现，用于向 SP 暴露额外端点或属性。</p>
 */
public interface SamlMetadataDescriptorUpdater
{
    /**
     * 根据映射器配置更新 SAML EntityDescriptor 元数据。
     * @param mapperModel 身份提供方映射器配置
     * @param descriptor 待修改的 EntityDescriptor
     */
}