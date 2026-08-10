package org.keycloak.saml.processing.core.parsers.saml.metadata;

import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.PDPDescriptorType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 元数据 {@code PDPDescriptor} 元素。
 * <p>读取策略决策点描述符属性，并解析授权服务与 NameID 格式等子元素。</p>
 *
 * @author mhajas
 */
public class SAMLPDPDescriptorParser extends SAMLRoleDecriptorTypeParser<PDPDescriptorType> {

    /** 单例实例。 */
    private static final SAMLPDPDescriptorParser INSTANCE = new SAMLPDPDescriptorParser();

    /** 构造并绑定 PDP_DESCRIPTOR 根元素。 */
    public SAMLPDPDescriptorParser() {
        super(SAMLMetadataQNames.PDP_DESCRIPTOR);
    }

    /** @return 解析器单例 */
    public static SAMLPDPDescriptorParser getInstance() {
        return INSTANCE;
    }

    /** 创建 PDP 描述符并读取协议支持枚举等属性。 */
    @Override
    protected PDPDescriptorType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        List<String> protocolEnum = StaxParserUtil.getRequiredStringListAttributeValue(element, SAMLMetadataQNames.ATTR_PROTOCOL_SUPPORT_ENUMERATION);
        PDPDescriptorType descriptor = new PDPDescriptorType(protocolEnum);

        parseOptionalArguments(element, descriptor);

        return descriptor;
    }

    /** 解析授权服务、断言 ID 请求服务及 NameID 格式子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, PDPDescriptorType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case AUTHZ_SERVICE:
            target.addAuthZService(SAMLAuthzServiceParser.getInstance().parse(xmlEventReader));
            break;

        case ASSERTION_ID_REQUEST_SERVICE:
            target.addAssertionIDRequestService(SAMLAssertinIDRequestServiceParser.getInstance().parse(xmlEventReader));
            break;

        case NAMEID_FORMAT:
            StaxParserUtil.advance(xmlEventReader);
            target.addNameIDFormat(StaxParserUtil.getElementText(xmlEventReader));
            break;

        default:
            super.processSubElement(xmlEventReader, target, element, elementDetail);
        }
    }
}
