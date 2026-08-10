package org.keycloak.saml.processing.core.parsers.saml.metadata;

import java.net.URI;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.EndpointType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * SAML 元数据 {@code EndpointType} 端点的抽象 STAX 解析器。
 * <p>读取 binding、location 及可选的 responseLocation 属性，子类指定具体端点元素名称。</p>
 *
 * @author mhajas
 */
public abstract class SAMLEndpointTypeParser extends AbstractStaxSamlMetadataParser<EndpointType> {

    /** 构造并指定期望的端点根元素。 */
    public SAMLEndpointTypeParser(SAMLMetadataQNames expectedStartElement) {
        super(expectedStartElement);
    }

    /** 创建端点对象并读取 binding、location 与 responseLocation 属性。 */
    @Override
    protected EndpointType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        String binding = StaxParserUtil.getRequiredAttributeValue(element, SAMLMetadataQNames.ATTR_BINDING);
        String location = StaxParserUtil.getRequiredAttributeValue(element, SAMLMetadataQNames.ATTR_LOCATION);

        EndpointType endpoint = new EndpointType(URI.create(binding), URI.create(location));

        String responseLocation = StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_RESPONSE_LOCATION);

        if (responseLocation != null) {
            endpoint.setResponseLocation(URI.create(responseLocation));
        }

        return endpoint;
    }

    /** EndpointType 不允许子元素，遇到未知标签则抛出解析异常。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, EndpointType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
    }
}
