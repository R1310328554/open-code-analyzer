package org.keycloak.saml.processing.core.parsers.saml.metadata;

/**
 * 解析 SAML 元数据中的 {@code ArtifactResolutionService} 元素。
 * <p>该端点用于通过 SAML Artifact 解析协议换取完整断言，继承带索引的端点解析逻辑。</p>
 *
 * @author mhajas
 */
public class SAMLArtifactResolutionServiceParser extends SAMLIndexedEndpointTypeParser {

    /** 单例实例。 */
    private static final SAMLArtifactResolutionServiceParser INSTANCE = new SAMLArtifactResolutionServiceParser();

    /** 构造并绑定 ARTIFACT_RESOLUTION_SERVICE 根元素。 */
    public SAMLArtifactResolutionServiceParser() {
        super(SAMLMetadataQNames.ARTIFACT_RESOLUTION_SERVICE);
    }

    /** 返回解析器单例。 */
    public static SAMLArtifactResolutionServiceParser getInstance() {
        return INSTANCE;
    }
}
