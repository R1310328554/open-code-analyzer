package org.keycloak.protocol.saml.mappers;

import java.util.List;
import java.util.stream.Collectors;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.provider.ScriptProviderMetadata;

/**
 * 已部署脚本 SAML 协议映射器。
 * <p>基于 {@link ScriptProviderMetadata} 元数据，在运行时加载并执行自定义脚本以转换 SAML 断言/响应。</p>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DeployedScriptSAMLProtocolMapper extends ScriptBasedMapper {

    /** 脚本提供方元数据（ID、名称、描述与脚本代码） */
    protected ScriptProviderMetadata metadata;

    /** @param metadata 脚本提供方元数据 */
    public DeployedScriptSAMLProtocolMapper(ScriptProviderMetadata metadata) {
        this.metadata = metadata;
    }

    /** 无参构造，供反射实例化使用 */
    public DeployedScriptSAMLProtocolMapper() {
        // 供反射调用
    }

    /** @return 脚本映射器标识 */
    @Override
    public String getId() {
        return metadata.getId();
    }

    /** @return 脚本映射器显示名称 */
    @Override
    public String getDisplayType() {
        return metadata.getName();
    }

    /** @return 脚本映射器说明 */
    @Override
    public String getHelpText() {
        return metadata.getDescription();
    }

    /** @param mapperModel 映射配置 @return 脚本源代码 */
    @Override
    protected String getScriptCode(ProtocolMapperModel mapperModel) {
        return metadata.getCode();
    }

    /** @return 配置属性列表（过滤内置 script 属性） */
    public List<ProviderConfigProperty> getConfigProperties() {
        return super.getConfigProperties().stream()
                .filter(providerConfigProperty -> !ProviderConfigProperty.SCRIPT_TYPE.equals(providerConfigProperty.getName())) // 过滤内置 script 属性
                .collect(Collectors.toList());
    }

    /** 设置脚本元数据 @param metadata 脚本提供方元数据 */
    public void setMetadata(ScriptProviderMetadata metadata) {
        this.metadata = metadata;
    }

    /** @return 脚本提供方元数据 */
    public ScriptProviderMetadata getMetadata() {
        return metadata;
    }
}
