package org.keycloak.saml.processing.core.parsers.saml.metadata;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.ContactType;
import org.keycloak.dom.saml.v2.metadata.ContactTypeType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.ATTR_CONTACT_TYPE;
import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.CONTACT_PERSON;

/**
 * 解析 SAML 元数据中的 {@code ContactPerson} 元素。
 * <p>读取联系人类型及公司、姓名、邮箱、电话等联系信息，并可包含扩展子元素。</p>
 *
 * @author mhajas
 */
public class SAMLContactPersonParser extends AbstractStaxSamlMetadataParser<ContactType> {

    /** 单例实例。 */
    private static final SAMLContactPersonParser INSTANCE = new SAMLContactPersonParser();

    /** 构造并绑定 CONTACT_PERSON 根元素。 */
    public SAMLContactPersonParser() {
        super(CONTACT_PERSON);
    }

    /** 返回解析器单例。 */
    public static SAMLContactPersonParser getInstance() {
        return INSTANCE;
    }

    /** 创建联系人对象并读取 contactType 属性。 */
    @Override
    protected ContactType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new ContactType(ContactTypeType.fromValue(StaxParserUtil.getRequiredAttributeValue(element, ATTR_CONTACT_TYPE)));
    }

    /** 解析公司、姓名、邮箱、电话及扩展等子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, ContactType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case COMPANY:
                StaxParserUtil.advance(xmlEventReader);
                target.setCompany(StaxParserUtil.getElementText(xmlEventReader));
                break;

            case GIVEN_NAME:
                StaxParserUtil.advance(xmlEventReader);
                target.setGivenName(StaxParserUtil.getElementText(xmlEventReader));
                break;

            case SURNAME:
                StaxParserUtil.advance(xmlEventReader);
                target.setSurName(StaxParserUtil.getElementText(xmlEventReader));
                break;

            case EMAIL_ADDRESS:
                StaxParserUtil.advance(xmlEventReader);
                target.addEmailAddress(StaxParserUtil.getElementText(xmlEventReader));
                break;

            case TELEPHONE_NUMBER:
                StaxParserUtil.advance(xmlEventReader);
                target.addTelephone(StaxParserUtil.getElementText(xmlEventReader));
                break;

            case EXTENSIONS:
                target.setExtensions(SAMLExtensionsParser.getInstance().parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
