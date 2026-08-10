package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据 {@code SingleLogoutService} 端点元素。
 * <p>继承 {@link SAMLEndpointTypeParser}，读取单点登出服务的 Binding 与 Location。</p>
 *
 * @author mhajas
 */
public class SAMLSingleLogoutServiceParser extends SAMLEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLSingleLogoutServiceParser INSTANCE = new SAMLSingleLogoutServiceParser();

    /** 构造并绑定 SINGLE_LOGOUT_SERVICE 根元素。 */
    public SAMLSingleLogoutServiceParser() {
        super(SAMLMetadataQNames.SINGLE_LOGOUT_SERVICE);
    }

    /** @return 解析器单例 */
    public static SAMLSingleLogoutServiceParser getInstance() {
        return INSTANCE;
    }
}
