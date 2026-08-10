package org.keycloak.protocol.oidc.mappers;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;

/**
 * 成对 subject 映射器配置辅助类：管理 sector identifier URI 与算法盐值等配置项。
 * <p>供 {@link SHA256PairwiseSubMapper} 等成对标识实现复用。</p>
 */
public class PairwiseSubMapperHelper {

    /** 配置键：sector identifier URI */
    public static final String SECTOR_IDENTIFIER_URI = "sectorIdentifierUri";
    /** 控制台标签键：sector identifier URI */
    public static final String SECTOR_IDENTIFIER_URI_LABEL = "sectorIdentifierUri.label";
    /** 帮助文本键：sector identifier URI */
    public static final String SECTOR_IDENTIFIER_URI_HELP_TEXT = "sectorIdentifierUri.tooltip";

    /** 配置键：成对 sub 哈希算法盐值 */
    public static final String PAIRWISE_SUB_ALGORITHM_SALT = "pairwiseSubAlgorithmSalt";
    /** 控制台标签键：算法盐值 */
    public static final String PAIRWISE_SUB_ALGORITHM_SALT_LABEL = "pairwiseSubAlgorithmSalt.label";
    /** 帮助文本键：算法盐值 */
    public static final String PAIRWISE_SUB_ALGORITHM_SALT_HELP_TEXT = "pairwiseSubAlgorithmSalt.tooltip";

    /** 从表示对象读取 sector identifier URI */
    public static String getSectorIdentifierUri(ProtocolMapperRepresentation mappingModel) {
        return mappingModel.getConfig().get(SECTOR_IDENTIFIER_URI);
    }

    /** 写入 sector identifier URI 到映射器模型 */
    public static void setSectorIdentifierUri(ProtocolMapperModel mappingModel, String sectorIdentifierUri) {
        mappingModel.getConfig().put(SECTOR_IDENTIFIER_URI, sectorIdentifierUri);
    }

    /** 从映射器模型读取 sector identifier URI */
    public static String getSectorIdentifierUri(ProtocolMapperModel mappingModel) {
        return mappingModel.getConfig().get(SECTOR_IDENTIFIER_URI);
    }

    /** 读取成对 sub 算法盐值 */
    public static String getSalt(ProtocolMapperModel mappingModel) {
        return mappingModel.getConfig().get(PAIRWISE_SUB_ALGORITHM_SALT);
    }

    /** 写入成对 sub 算法盐值 */
    public static void setSalt(ProtocolMapperModel mappingModel, String salt) {
        mappingModel.getConfig().put(PAIRWISE_SUB_ALGORITHM_SALT, salt);
    }

    /** 构建 sector identifier URI 配置属性定义 */
    public static ProviderConfigProperty createSectorIdentifierConfig() {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(SECTOR_IDENTIFIER_URI);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setLabel(SECTOR_IDENTIFIER_URI_LABEL);
        property.setHelpText(SECTOR_IDENTIFIER_URI_HELP_TEXT);
        return property;
    }

    /** 构建算法盐值配置属性定义 */
    public static ProviderConfigProperty createSaltConfig() {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(PAIRWISE_SUB_ALGORITHM_SALT);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setLabel(PAIRWISE_SUB_ALGORITHM_SALT_LABEL);
        property.setHelpText(PAIRWISE_SUB_ALGORITHM_SALT_HELP_TEXT);
        return property;
    }
}
