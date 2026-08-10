package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据 {@code ManageNameIDService} 端点元素。
 * <p>继承 {@link SAMLEndpointTypeParser}，读取 NameID 管理服务的 Binding 与 Location。</p>
 *
 * @author mhajas
 */
public class SAMLManageNameIDServiceParser extends SAMLEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLManageNameIDServiceParser INSTANCE = new SAMLManageNameIDServiceParser();

    /** 构造并绑定 MANAGE_NAMEID_SERVICE 根元素。 */
    public SAMLManageNameIDServiceParser() {
        super(SAMLMetadataQNames.MANAGE_NAMEID_SERVICE);
    }

    /** @return 解析器单例 */
    public static SAMLManageNameIDServiceParser getInstance() {
        return INSTANCE;
    }
}
