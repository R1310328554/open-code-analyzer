package org.keycloak.saml.processing.core.parsers.saml.mdattr;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.mdattr.EntityAttributes;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;
import org.keycloak.saml.processing.core.parsers.saml.assertion.SAMLAssertionParser;
import org.keycloak.saml.processing.core.parsers.saml.assertion.SAMLAttributeParser;
import org.keycloak.saml.processing.core.parsers.saml.metadata.AbstractStaxSamlMetadataParser;
import org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames;

/**
 * 解析 SAML 元数据扩展 {@code EntityAttributes} 元素。
 * <p>收集实体级 Attribute 与嵌套 Assertion 子元素。</p>
 */
public class SAMLEntityAttributesParser extends AbstractStaxSamlMetadataParser<EntityAttributes> {
    /** 单例实例。 */
    private static final SAMLEntityAttributesParser INSTANCE = new SAMLEntityAttributesParser();

    /** 私有构造，绑定 ENTITY_ATTRIBUTES 根元素。 */
    private SAMLEntityAttributesParser() {
        super(SAMLMetadataQNames.ENTITY_ATTRIBUTES);
    }

    /** @return 解析器单例 */
    public static SAMLEntityAttributesParser getInstance() {
        return INSTANCE;
    }

    /** 创建空的实体属性容器。 */
    @Override
    protected EntityAttributes instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new EntityAttributes();
    }

    /** 解析 Attribute 或 Assertion 子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, EntityAttributes target, SAMLMetadataQNames element,
        StartElement elementDetail) throws ParsingException {
        switch (element) {
            case ATTRIBUTE:
                target.addAttribute(SAMLAttributeParser.getInstance().parse(xmlEventReader));
                break;
            case ASSERTION:
                target.addAssertion(SAMLAssertionParser.getInstance().parse(xmlEventReader));
                break;
            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
