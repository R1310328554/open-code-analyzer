package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据中的 {@code AuthnQueryService} 元素。
 * <p>认证权威通过该端点响应认证查询请求，继承通用 {@link EndpointType} 端点解析逻辑。</p>
 *
 * @author mhajas
 */
public class SAMLAuthnQueryServiceParser extends SAMLEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLAuthnQueryServiceParser INSTANCE = new SAMLAuthnQueryServiceParser();

    /** 构造并绑定 AUTHN_QUERY_SERVICE 根元素。 */
    public SAMLAuthnQueryServiceParser() {
        super(SAMLMetadataQNames.AUTHN_QUERY_SERVICE);
    }

    /** 返回解析器单例。 */
    public static SAMLAuthnQueryServiceParser getInstance() {
        return INSTANCE;
    }
}
