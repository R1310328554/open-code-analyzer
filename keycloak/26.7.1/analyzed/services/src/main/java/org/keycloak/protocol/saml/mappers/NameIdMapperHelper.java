package org.keycloak.protocol.saml.mappers;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;

/**
 * SAML NameID 映射器配置辅助类。
 * <p>为实现 {@link NameIDMapper} 的映射器提供 NameID Format 配置属性。</p>
 */
public class NameIdMapperHelper {

    /** 管理控制台 NameID 映射器分类标签 */
    public static final String NAMEID_MAPPER_CATEGORY = "NameID Mapper";

    // 实现 NameIDMapper 的类需包含以下配置属性
    /** 配置键：NameID Format URI */
    public static final String MAPPER_NAMEID_FORMAT = "mapper.nameid.format";
    /** NameID Format 配置项标签键 */
    public static final String MAPPER_NAMEID_FORMAT_LABEL = "name-id-format";
    /** NameID Format 配置项帮助文本键 */
    public static final String MAPPER_NAMEID_FORMAT_HELP_TEXT = "mapper.nameid.format.tooltip";

    /** 向配置列表追加 NameID Format 选项（unspecified/email/persistent/transient） @param configProperties 目标配置列表 */
    public static void setConfigProperties(List<ProviderConfigProperty> configProperties) {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(NameIdMapperHelper.MAPPER_NAMEID_FORMAT);
        property.setLabel(NameIdMapperHelper.MAPPER_NAMEID_FORMAT_LABEL);
        property.setHelpText(NameIdMapperHelper.MAPPER_NAMEID_FORMAT_HELP_TEXT);
        List<String> types = new ArrayList<String>();
        types.add(JBossSAMLURIConstants.NAMEID_FORMAT_UNSPECIFIED.get());
        types.add(JBossSAMLURIConstants.NAMEID_FORMAT_EMAIL.get());
        types.add(JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get());
        types.add(JBossSAMLURIConstants.NAMEID_FORMAT_TRANSIENT.get());
        property.setType(ProviderConfigProperty.LIST_TYPE);
        property.setOptions(types);
        configProperties.add(property);
    }
}
