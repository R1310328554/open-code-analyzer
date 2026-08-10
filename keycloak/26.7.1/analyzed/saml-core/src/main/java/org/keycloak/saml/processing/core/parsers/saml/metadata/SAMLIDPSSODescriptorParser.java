package org.keycloak.saml.processing.core.parsers.saml.metadata;

import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.IDPSSODescriptorType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.IDP_SSO_DESCRIPTOR;

/**
 * 解析 SAML 元数据 {@code IDPSSODescriptor} 元素。
 * <p>读取 IdP SSO 描述符属性，并解析单点登录、NameID 映射等服务端点。</p>
 *
 * @author mhajas
 */
public class SAMLIDPSSODescriptorParser extends SAMLSSODescriptorTypeParser<IDPSSODescriptorType> {

    /** 单例实例。 */
    private static final SAMLIDPSSODescriptorParser INSTANCE = new SAMLIDPSSODescriptorParser();

    /** 私有构造，绑定 IDP_SSO_DESCRIPTOR 根元素。 */
    private SAMLIDPSSODescriptorParser() {
        super(IDP_SSO_DESCRIPTOR);
    }

    /** @return 解析器单例 */
    public static SAMLIDPSSODescriptorParser getInstance() {
        return INSTANCE;
    }

    /** 创建 IdP SSO 描述符并读取协议支持与签名要求等属性。 */
    @Override
    protected IDPSSODescriptorType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        List<String> protocolEnum = StaxParserUtil.getRequiredStringListAttributeValue(element, SAMLMetadataQNames.ATTR_PROTOCOL_SUPPORT_ENUMERATION);
        IDPSSODescriptorType descriptor = new IDPSSODescriptorType(protocolEnum);

        // 角色描述符可选属性
        parseOptionalArguments(element, descriptor);

        // IDPSSODescriptor 可选属性
        Boolean wantAuthnRequestsSigned = StaxParserUtil.getBooleanAttributeValue(element, SAMLMetadataQNames.ATTR_WANT_AUTHN_REQUESTS_SIGNED);
        if (wantAuthnRequestsSigned != null) {
            descriptor.setWantAuthnRequestsSigned(wantAuthnRequestsSigned);
        }

        return descriptor;
    }

    /** 解析 SSO、NameID 映射、断言 ID 请求等服务及属性配置子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, IDPSSODescriptorType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case SINGLE_SIGNON_SERVICE:
                target.addSingleSignOnService(SAMLSingleSignOnServiceParser.getInstance().parse(xmlEventReader));
                break;

            case NAMEID_MAPPING_SERVICE:
                target.addNameIDMappingService(SAMLNameIDMappingServiceParser.getInstance().parse(xmlEventReader));
                break;

            case ASSERTION_ID_REQUEST_SERVICE:
                target.addAssertionIDRequestService(SAMLAssertinIDRequestServiceParser.getInstance().parse(xmlEventReader));
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
