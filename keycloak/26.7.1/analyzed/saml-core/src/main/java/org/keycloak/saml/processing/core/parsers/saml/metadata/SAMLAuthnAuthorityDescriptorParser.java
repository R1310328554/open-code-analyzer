package org.keycloak.saml.processing.core.parsers.saml.metadata;

import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.AuthnAuthorityDescriptorType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 元数据中的 {@code AuthnAuthorityDescriptor} 元素。
 * <p>描述认证权威实体提供的认证查询服务、断言 ID 请求服务及支持的 NameID 格式。</p>
 *
 * @author mhajas
 */
public class SAMLAuthnAuthorityDescriptorParser extends SAMLRoleDecriptorTypeParser<AuthnAuthorityDescriptorType> {

    /** 单例实例。 */
    private static final SAMLAuthnAuthorityDescriptorParser INSTANCE = new SAMLAuthnAuthorityDescriptorParser();

    /** 构造并绑定 AUTHN_AUTHORITY_DESCRIPTOR 根元素。 */
    public SAMLAuthnAuthorityDescriptorParser() {
        super(SAMLMetadataQNames.AUTHN_AUTHORITY_DESCRIPTOR);
    }

    /** 返回解析器单例。 */
    public static SAMLAuthnAuthorityDescriptorParser getInstance() {
        return INSTANCE;
    }

    /** 创建认证权威描述符并读取协议支持枚举等属性。 */
    @Override
    protected AuthnAuthorityDescriptorType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        List<String> protocolEnum = StaxParserUtil.getRequiredStringListAttributeValue(element, SAMLMetadataQNames.ATTR_PROTOCOL_SUPPORT_ENUMERATION);
        AuthnAuthorityDescriptorType descriptor = new AuthnAuthorityDescriptorType(protocolEnum);

        parseOptionalArguments(element, descriptor);

        return descriptor;
    }

    /** 分发并解析认证查询服务、断言 ID 请求服务及 NameID 格式子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, AuthnAuthorityDescriptorType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
        case AUTHN_QUERY_SERVICE:
            target.addAuthnQueryService(SAMLAuthnQueryServiceParser.getInstance().parse(xmlEventReader));
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
