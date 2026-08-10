package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据中的 {@code AttributeService} 元素。
 * <p>属性权威通过该端点响应属性查询请求，继承通用 {@link EndpointType} 端点解析逻辑。</p>
 *
 * @author mhajas
 */
public class SAMLAttributeServiceParser extends SAMLEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLAttributeServiceParser INSTANCE = new SAMLAttributeServiceParser();

    /** 构造并绑定 ATTRIBUTE_SERVICE 根元素。 */
    public SAMLAttributeServiceParser() {
        super(SAMLMetadataQNames.ATTRIBUTE_SERVICE);
    }

    /** 返回解析器单例。 */
    public static SAMLAttributeServiceParser getInstance() {
        return INSTANCE;
    }
}
