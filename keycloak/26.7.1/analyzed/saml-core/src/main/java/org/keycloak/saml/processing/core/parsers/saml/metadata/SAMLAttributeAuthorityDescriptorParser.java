package org.keycloak.saml.processing.core.parsers.saml.metadata;

import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.AttributeAuthorityDescriptorType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 元数据中的 {@code AttributeAuthorityDescriptor} 元素。
 * <p>描述属性权威实体提供的服务端点、NameID 格式、属性配置文件及支持的属性列表。</p>
 *
 * @author mhajas
 */
public class SAMLAttributeAuthorityDescriptorParser extends SAMLRoleDecriptorTypeParser<AttributeAuthorityDescriptorType> {

    /** 单例实例。 */
    private static final SAMLAttributeAuthorityDescriptorParser INSTANCE = new SAMLAttributeAuthorityDescriptorParser();

    /** 构造并绑定 ATTRIBUTE_AUTHORITY_DESCRIPTOR 根元素。 */
    public SAMLAttributeAuthorityDescriptorParser() {
        super(SAMLMetadataQNames.ATTRIBUTE_AUTHORITY_DESCRIPTOR);
    }

    /** 返回解析器单例。 */
    public static SAMLAttributeAuthorityDescriptorParser getInstance() {
        return INSTANCE;
    }

    /** 创建属性权威描述符并读取协议支持枚举等属性。 */
    @Override
    protected AttributeAuthorityDescriptorType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        List<String> protocolEnum = StaxParserUtil.getRequiredStringListAttributeValue(element, SAMLMetadataQNames.ATTR_PROTOCOL_SUPPORT_ENUMERATION);
        AttributeAuthorityDescriptorType descriptor = new AttributeAuthorityDescriptorType(protocolEnum);

        parseOptionalArguments(element, descriptor);

        return descriptor;
    }

    /** 分发并解析属性服务、断言 ID 请求服务、NameID 格式、属性配置文件及属性子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, AttributeAuthorityDescriptorType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case ATTRIBUTE_SERVICE:
            target.addAttributeService(SAMLAttributeServiceParser.getInstance().parse(xmlEventReader));
            break;

        case ASSERTION_ID_REQUEST_SERVICE:
            target.addAssertionIDRequestService(SAMLAssertinIDRequestServiceParser.getInstance().parse(xmlEventReader));
            break;

        case NAMEID_FORMAT:
            StaxParserUtil.advance(xmlEventReader);
            target.addNameIDFormat(StaxParserUtil.getElementText(xmlEventReader));
            break;

        case ATTRIBUTE_PROFILE:
            StaxParserUtil.advance(xmlEventReader);
            target.addAttributeProfile(StaxParserUtil.getElementText(xmlEventReader));
            break;

        case ATTRIBUTE:
            target.addAttribute(SAMLAttributeParser.getInstance().parse(xmlEventReader));
            break;

        default:
            super.processSubElement(xmlEventReader, target, element, elementDetail);
        }
    }
}
