package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据中的 {@code AuthzService} 元素。
 * <p>PDP 通过该端点响应授权决策查询，继承通用 {@link EndpointType} 端点解析逻辑。</p>
 *
 * @author mhajas
 */
public class SAMLAuthzServiceParser extends SAMLEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLAuthzServiceParser INSTANCE = new SAMLAuthzServiceParser();

    /** 构造并绑定 AUTHZ_SERVICE 根元素。 */
    public SAMLAuthzServiceParser() {
        super(SAMLMetadataQNames.AUTHZ_SERVICE);
    }

    /** 返回解析器单例。 */
    public static SAMLAuthzServiceParser getInstance() {
        return INSTANCE;
    }
}
