package org.keycloak.saml.processing.core.parsers.saml.metadata;

import java.net.URI;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.LocalizedNameType;
import org.keycloak.dom.saml.v2.metadata.LocalizedURIType;
import org.keycloak.dom.saml.v2.metadata.OrganizationType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.ATTR_LANG;
import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.ORGANIZATION;

/**
 * 解析 SAML 元数据 {@code Organization} 元素。
 * <p>读取组织名称、显示名称、URL 等本地化信息及扩展子元素。</p>
 *
 * @author mhajas
 */
public class SAMLOrganizationParser extends AbstractStaxSamlMetadataParser<OrganizationType> {

    /** 单例实例。 */
    private static final SAMLOrganizationParser INSTANCE = new SAMLOrganizationParser();

    /** 构造并绑定 ORGANIZATION 根元素。 */
    public SAMLOrganizationParser() {
        super(ORGANIZATION);
    }

    /** @return 解析器单例 */
    public static SAMLOrganizationParser getInstance() {
        return INSTANCE;
    }

    /** 创建空的组织信息对象。 */
    @Override
    protected OrganizationType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new OrganizationType();
    }

    /** 解析组织名称、显示名称、URL 及 Extensions 子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, OrganizationType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case ORGANIZATION_NAME:
                LocalizedNameType orgName = new LocalizedNameType(StaxParserUtil.getAttributeValue(elementDetail, ATTR_LANG));
                StaxParserUtil.advance(xmlEventReader);
                orgName.setValue(StaxParserUtil.getElementText(xmlEventReader));
                target.addOrganizationName(orgName);
                break;

            case ORGANIZATION_DISPLAY_NAME:
                LocalizedNameType orgDispName = new LocalizedNameType(StaxParserUtil.getAttributeValue(elementDetail, ATTR_LANG));
                StaxParserUtil.advance(xmlEventReader);
                orgDispName.setValue(StaxParserUtil.getElementText(xmlEventReader));
                target.addOrganizationDisplayName(orgDispName);
                break;

            case ORGANIZATION_URL:
            case ORGANIZATION_URL_ALT:
                LocalizedURIType orgURL = new LocalizedURIType(StaxParserUtil.getAttributeValue(elementDetail, ATTR_LANG));
                StaxParserUtil.advance(xmlEventReader);
                orgURL.setValue(URI.create(StaxParserUtil.getElementText(xmlEventReader)));
                target.addOrganizationURL(orgURL);
                break;

            case EXTENSIONS:
                target.setExtensions(SAMLExtensionsParser.getInstance().parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
