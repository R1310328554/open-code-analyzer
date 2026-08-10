package org.keycloak.saml.processing.core.parsers.saml.metadata;

import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.SPSSODescriptorType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.SP_SSO_DESCRIPTOR;

/**
 * 解析 SAML 元数据 {@code SPSSODescriptor} 元素。
 * <p>读取 SP SSO 描述符属性，并解析断言消费服务与属性消费服务子元素。</p>
 *
 * @author mhajas
 */
public class SAMLSPSSODescriptorParser extends SAMLSSODescriptorTypeParser<SPSSODescriptorType> {

    /** 单例实例。 */
    private static final SAMLSPSSODescriptorParser INSTANCE = new SAMLSPSSODescriptorParser();

    /** 私有构造，绑定 SP_SSO_DESCRIPTOR 根元素。 */
    private SAMLSPSSODescriptorParser() {
        super(SP_SSO_DESCRIPTOR);
    }

    /** @return 解析器单例 */
    public static SAMLSPSSODescriptorParser getInstance() {
        return INSTANCE;
    }

    /** 创建 SP SSO 描述符并读取协议支持与签名要求等属性。 */
    @Override
    protected SPSSODescriptorType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        List<String> protocolEnum = StaxParserUtil.getRequiredStringListAttributeValue(element, SAMLMetadataQNames.ATTR_PROTOCOL_SUPPORT_ENUMERATION);
        SPSSODescriptorType descriptor = new SPSSODescriptorType(protocolEnum);

        // 角色描述符可选属性
        parseOptionalArguments(element, descriptor);

        // SPSSODescriptor 可选属性
        Boolean authnRequestsSigned = StaxParserUtil.getBooleanAttributeValue(element, SAMLMetadataQNames.ATTR_AUTHN_REQUESTS_SIGNED);
        if (authnRequestsSigned != null) {
            descriptor.setAuthnRequestsSigned(authnRequestsSigned);
        }

        Boolean wantAssertionSigned = StaxParserUtil.getBooleanAttributeValue(element, SAMLMetadataQNames.ATTR_WANT_ASSERTIONS_SIGNED);
        if (wantAssertionSigned != null) {
            descriptor.setWantAssertionsSigned(wantAssertionSigned);
        }

        return descriptor;
    }

    /** 解析断言消费服务与属性消费服务子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, SPSSODescriptorType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case ASSERTION_CONSUMER_SERVICE:
                target.addAssertionConsumerService(SAMLAssertionConsumerServiceParser.getInstance().parse(xmlEventReader));
                break;

            case ATTRIBUTE_CONSUMING_SERVICE:
                target.addAttributeConsumerService(SAMLAttributeConsumingServiceParser.getInstance().parse(xmlEventReader));
                break;

            default:
                super.processSubElement(xmlEventReader, target, element, elementDetail);
        }
    }
}
