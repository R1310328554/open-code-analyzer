package org.keycloak.saml.processing.core.parsers.saml.metadata;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.RoleDescriptorType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import org.w3c.dom.Element;

/**
 * 解析 SAML 元数据角色描述符类型元素的抽象基类。
 * <p>处理 KeyDescriptor、Signature、Extensions、Organization 等通用子元素。</p>
 *
 * @param <T> 角色描述符 Java 类型
 * @author mhajas
 */
public abstract class SAMLRoleDecriptorTypeParser<T extends RoleDescriptorType> extends AbstractStaxSamlMetadataParser<T> {

    /** 构造并指定期望的角色描述符根元素。 */
    public SAMLRoleDecriptorTypeParser(SAMLMetadataQNames expectedStartElement) {
        super(expectedStartElement);
    }

    /** 解析角色描述符的可选属性（ID、validUntil、cacheDuration）。 */
    protected void parseOptionalArguments(StartElement element, RoleDescriptorType descriptor) throws ParsingException {
        descriptor.setID(StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_ID));
        descriptor.setValidUntil(StaxParserUtil.getXmlTimeAttributeValue(element, SAMLMetadataQNames.ATTR_VALID_UNTIL));
        descriptor.setCacheDuration(StaxParserUtil.getXmlDurationAttributeValue(element, SAMLMetadataQNames.ATTR_CACHE_DURATION));
    }

    /** 分发处理密钥、签名、扩展、组织及联系人等通用子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, T target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case KEY_DESCRIPTOR:
                target.addKeyDescriptor(SAMLKeyDescriptorParser.getInstance().parse(xmlEventReader));
                break;

            case SIGNATURE:
                Element sig = StaxParserUtil.getDOMElement(xmlEventReader);
                target.setSignature(sig);
                break;

            case EXTENSIONS:
                target.setExtensions(SAMLExtensionsParser.getInstance().parse(xmlEventReader));
                break;

            case ORGANIZATION:
                target.setOrganization(SAMLOrganizationParser.getInstance().parse(xmlEventReader));
                break;

            case CONTACT_PERSON:
                target.addContactPerson(SAMLContactPersonParser.getInstance().parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
