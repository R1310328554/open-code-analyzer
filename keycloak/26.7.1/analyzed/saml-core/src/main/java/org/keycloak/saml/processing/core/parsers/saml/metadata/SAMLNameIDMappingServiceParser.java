package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据 {@code NameIDMappingService} 端点元素。
 * <p>继承 {@link SAMLEndpointTypeParser}，读取 NameID 映射服务的 Binding 与 Location。</p>
 *
 * @author mhajas
 */
public class SAMLNameIDMappingServiceParser extends SAMLEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLNameIDMappingServiceParser INSTANCE = new SAMLNameIDMappingServiceParser();

    /** 构造并绑定 NAMEID_MAPPING_SERVICE 根元素。 */
    public SAMLNameIDMappingServiceParser() {
        super(SAMLMetadataQNames.NAMEID_MAPPING_SERVICE);
    }

    /** @return 解析器单例 */
    public static SAMLNameIDMappingServiceParser getInstance() {
        return INSTANCE;
    }
}
