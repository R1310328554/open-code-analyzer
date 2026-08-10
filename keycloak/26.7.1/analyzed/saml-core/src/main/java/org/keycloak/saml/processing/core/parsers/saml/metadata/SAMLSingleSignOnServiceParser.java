package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据 {@code SingleSignOnService} 端点元素。
 * <p>继承 {@link SAMLEndpointTypeParser}，读取单点登录服务的 Binding 与 Location。</p>
 *
 * @author mhajas
 */
public class SAMLSingleSignOnServiceParser extends SAMLEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLSingleSignOnServiceParser INSTANCE = new SAMLSingleSignOnServiceParser();

    /** 构造并绑定 SINGLE_SIGNON_SERVICE 根元素。 */
    public SAMLSingleSignOnServiceParser() {
        super(SAMLMetadataQNames.SINGLE_SIGNON_SERVICE);
    }

    /** @return 解析器单例 */
    public static SAMLSingleSignOnServiceParser getInstance() {
        return INSTANCE;
    }
}
