package org.keycloak.saml.processing.core.parsers.saml.metadata;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.AttributeConsumingServiceType;
import org.keycloak.dom.saml.v2.metadata.LocalizedNameType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.ATTR_LANG;


/**
 * 解析 SAML 元数据中的 {@code AttributeConsumingService} 元素。
 * <p>描述 SP 期望从 IdP 获取的属性集合，包括服务名称、描述及请求的各属性定义。</p>
 *
 * @author mhajas
 */
public class SAMLAttributeConsumingServiceParser extends AbstractStaxSamlMetadataParser<AttributeConsumingServiceType> {

    /** 单例实例。 */
    private static final SAMLAttributeConsumingServiceParser INSTANCE = new SAMLAttributeConsumingServiceParser();

    /** 构造并绑定 ATTRIBUTE_CONSUMING_SERVICE 根元素。 */
    public SAMLAttributeConsumingServiceParser() {
        super(SAMLMetadataQNames.ATTRIBUTE_CONSUMING_SERVICE);
    }

    /** 返回解析器单例。 */
    public static SAMLAttributeConsumingServiceParser getInstance() {
        return INSTANCE;
    }

    /** 创建属性消费服务并读取 index 与 isDefault 属性。 */
    @Override
    protected AttributeConsumingServiceType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        int index = Integer.parseInt(StaxParserUtil.getRequiredAttributeValue(element, SAMLMetadataQNames.ATTR_INDEX));

        AttributeConsumingServiceType service = new AttributeConsumingServiceType(index);
        service.setIsDefault(StaxParserUtil.getBooleanAttributeValue(element, SAMLMetadataQNames.ATTR_IS_DEFAULT));

        return service;
    }

    /** 解析服务名称、服务描述及请求属性等子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, AttributeConsumingServiceType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case SERVICE_NAME:
                LocalizedNameType serviceName = new LocalizedNameType(StaxParserUtil.getAttributeValue(elementDetail, ATTR_LANG));
                StaxParserUtil.advance(xmlEventReader);
                serviceName.setValue(StaxParserUtil.getElementText(xmlEventReader));
                target.addServiceName(serviceName);
                break;

            case SERVICE_DESCRIPTION:
                LocalizedNameType serviceDescription = new LocalizedNameType(StaxParserUtil.getAttributeValue(elementDetail, ATTR_LANG));
                StaxParserUtil.advance(xmlEventReader);
                serviceDescription.setValue(StaxParserUtil.getElementText(xmlEventReader));
                target.addServiceDescription(serviceDescription);
                break;

            case REQUESTED_ATTRIBUTE:
                target.addRequestedAttribute(SAMLRequestedAttributeParser.getInstance().parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
