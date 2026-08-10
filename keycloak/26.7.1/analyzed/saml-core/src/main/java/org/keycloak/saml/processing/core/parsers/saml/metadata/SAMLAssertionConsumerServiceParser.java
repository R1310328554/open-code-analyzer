package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据中的 {@code AssertionConsumerService} 元素。
 * <p>SP 通过该端点接收 IdP 返回的 SAML 断言，继承带索引与默认标志的端点解析逻辑。</p>
 *
 * @author mhajas
 */
public class SAMLAssertionConsumerServiceParser extends SAMLIndexedEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLAssertionConsumerServiceParser INSTANCE = new SAMLAssertionConsumerServiceParser();

    /** 构造并绑定 ASSERTION_CONSUMER_SERVICE 根元素。 */
    public SAMLAssertionConsumerServiceParser() {
        super(SAMLMetadataQNames.ASSERTION_CONSUMER_SERVICE);
    }

    /** 返回解析器单例。 */
    public static SAMLAssertionConsumerServiceParser getInstance() {
        return INSTANCE;
    }
}
