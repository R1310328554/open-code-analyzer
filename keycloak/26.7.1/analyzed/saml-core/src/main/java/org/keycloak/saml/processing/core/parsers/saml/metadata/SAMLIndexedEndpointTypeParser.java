package org.keycloak.saml.processing.core.parsers.saml.metadata;

import java.net.URI;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.IndexedEndpointType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 元数据带索引端点类型元素的抽象基类。
 * <p>读取 Binding、Location、index、isDefault 等属性，构建 {@link IndexedEndpointType}。</p>
 *
 * @author mhajas
 */
public abstract class SAMLIndexedEndpointTypeParser extends AbstractStaxSamlMetadataParser<IndexedEndpointType> {

    /** 构造并指定期望的带索引端点根元素。 */
    public SAMLIndexedEndpointTypeParser(SAMLMetadataQNames expectedStartElement) {
        super(expectedStartElement);
    }

    /** 从起始元素属性创建带索引端点对象。 */
    @Override
    protected IndexedEndpointType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        String binding = StaxParserUtil.getRequiredAttributeValue(element, SAMLMetadataQNames.ATTR_BINDING);
        String location = StaxParserUtil.getRequiredAttributeValue(element, SAMLMetadataQNames.ATTR_LOCATION);

        IndexedEndpointType endpoint = new IndexedEndpointType(URI.create(binding), URI.create(location));

        Boolean isDefault = StaxParserUtil.getBooleanAttributeValue(element, SAMLMetadataQNames.ATTR_IS_DEFAULT);
        if (isDefault != null) {
            endpoint.setIsDefault(isDefault);
        }
        
        Integer index = StaxParserUtil.getIntegerAttributeValue(element, SAMLMetadataQNames.ATTR_INDEX);
        if (index != null)
            endpoint.setIndex(index);

        // EndpointType 属性
        String responseLocation = StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_RESPONSE_LOCATION);

        if (responseLocation != null) {
            endpoint.setResponseLocation(URI.create(responseLocation));
        }

        return endpoint;
    }

    /** 带索引端点不允许子元素，遇到未知标签则抛出解析异常。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, IndexedEndpointType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
    }
}
