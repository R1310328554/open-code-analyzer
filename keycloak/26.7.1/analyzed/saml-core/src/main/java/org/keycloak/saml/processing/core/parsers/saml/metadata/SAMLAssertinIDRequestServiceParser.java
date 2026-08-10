package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据中的 {@code AssertionIDRequestService} 元素。
 * <p>该端点用于按断言 ID 查询断言，继承通用 {@link EndpointType} 端点解析逻辑。</p>
 *
 * @author mhajas
 */
public class SAMLAssertinIDRequestServiceParser extends SAMLEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLAssertinIDRequestServiceParser INSTANCE = new SAMLAssertinIDRequestServiceParser();

    /** 构造并绑定 ASSERTION_ID_REQUEST_SERVICE 根元素。 */
    public SAMLAssertinIDRequestServiceParser() {
        super(SAMLMetadataQNames.ASSERTION_ID_REQUEST_SERVICE);
    }

    /** 返回解析器单例。 */
    public static SAMLAssertinIDRequestServiceParser getInstance() {
        return INSTANCE;
    }
}
