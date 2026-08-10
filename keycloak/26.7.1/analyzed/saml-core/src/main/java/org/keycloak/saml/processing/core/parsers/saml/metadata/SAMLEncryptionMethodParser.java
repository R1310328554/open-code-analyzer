package org.keycloak.saml.processing.core.parsers.saml.metadata;

import java.math.BigInteger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.xmlsec.w3.xmlenc.EncryptionMethodType;
import org.keycloak.saml.common.constants.GeneralConstants;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

import static org.keycloak.saml.processing.core.parsers.saml.metadata.SAMLMetadataQNames.ENCRYPTION_METHOD;

/**
 * 解析 SAML 元数据中的 {@code EncryptionMethod} 元素。
 * <p>读取加密算法 URI，并解析 KeySize、OAEPParams 及其他 XML 加密扩展子元素。</p>
 *
 * @author mhajas
 */
public class SAMLEncryptionMethodParser extends AbstractStaxSamlMetadataParser<EncryptionMethodType> {

    /** 单例实例。 */
    private static final SAMLEncryptionMethodParser INSTANCE = new SAMLEncryptionMethodParser();

    /** 构造并绑定 ENCRYPTION_METHOD 根元素。 */
    public SAMLEncryptionMethodParser() {
        super(ENCRYPTION_METHOD);
    }

    /** 返回解析器单例。 */
    public static SAMLEncryptionMethodParser getInstance() {
        return INSTANCE;
    }

    /** 创建加密方法对象并读取 algorithm 属性。 */
    @Override
    protected EncryptionMethodType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        return new EncryptionMethodType(StaxParserUtil.getRequiredAttributeValue(element, SAMLMetadataQNames.ATTR_ALGORITHM));
    }

    /** 解析 KeySize、OAEPParams 及未识别的扩展子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, EncryptionMethodType target, SAMLMetadataQNames element, StartElement elementDetail) throws ParsingException {
        switch(element) {
            case KEY_SIZE:
                {
                    StaxParserUtil.advance(xmlEventReader);
                    BigInteger keySize = BigInteger.valueOf(Long.parseLong(StaxParserUtil.getElementText(xmlEventReader)));

                    EncryptionMethodType.EncryptionMethod encMethod = target.getEncryptionMethod();
                    if (encMethod == null) {
                        encMethod = new EncryptionMethodType.EncryptionMethod();
                        target.setEncryptionMethod(encMethod);
                    }

                    encMethod.setKeySize(keySize);
                }
                break;

            case OAEP_PARAMS:
                {
                    StaxParserUtil.advance(xmlEventReader);
                    byte[] OAEPparams = StaxParserUtil.getElementText(xmlEventReader).getBytes(GeneralConstants.SAML_CHARSET);
                    EncryptionMethodType.EncryptionMethod encMethod = target.getEncryptionMethod();
                    if (encMethod == null){
                        encMethod = new EncryptionMethodType.EncryptionMethod();
                        target.setEncryptionMethod(encMethod);
                    }

                    encMethod.setOAEPparams(OAEPparams);
                }
                break;

            default:
                {
                    EncryptionMethodType.EncryptionMethod encMethod = target.getEncryptionMethod();
                    if (encMethod == null) {
                        encMethod = new EncryptionMethodType.EncryptionMethod();
                        target.setEncryptionMethod(encMethod);
                    }
                    encMethod.addAny(StaxParserUtil.getDOMElement(xmlEventReader));
                }

        }
    }
}
