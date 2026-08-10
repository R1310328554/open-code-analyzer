package org.keycloak.saml.processing.core.parsers.saml.metadata;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.metadata.KeyDescriptorType;
import org.keycloak.dom.saml.v2.metadata.KeyTypes;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.KEY_DESCRIPTOR;

/**
 * 解析 SAML 元数据 {@code KeyDescriptor} 元素。
 * <p>读取密钥用途（use）属性，并解析 KeyInfo 与 EncryptionMethod 子元素。</p>
 *
 * @author mhajas
 */
public class SAMLKeyDescriptorParser extends AbstractStaxSamlMetadataParser<KeyDescriptorType> {

    /** 单例实例。 */
    private static final SAMLKeyDescriptorParser INSTANCE = new SAMLKeyDescriptorParser();

    /** 构造并绑定 KEY_DESCRIPTOR 根元素。 */
    public SAMLKeyDescriptorParser() {
        super(KEY_DESCRIPTOR);
    }

    /** @return 解析器单例 */
    public static SAMLKeyDescriptorParser getInstance() {
        return INSTANCE;
    }

    /** 创建密钥描述符并读取 use 属性。 */
    @Override
    protected KeyDescriptorType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        KeyDescriptorType keyDescriptor = new KeyDescriptorType();

        String use = StaxParserUtil.getAttributeValue(element, SAMLMetadataQNames.ATTR_USE);

        if (use != null && !use.isEmpty()) {
            keyDescriptor.setUse(KeyTypes.fromValue(use));
        }

        return keyDescriptor;
    }

    /** 解析 KeyInfo 或 EncryptionMethod 子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, KeyDescriptorType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch(element) {
            case KEY_INFO:
                target.setKeyInfo(StaxParserUtil.getDOMElement(xmlEventReader));
                break;

            case ENCRYPTION_METHOD:
                target.addEncryptionMethod(SAMLEncryptionMethodParser.getInstance().parse(xmlEventReader));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
