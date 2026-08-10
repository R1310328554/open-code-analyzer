package org.keycloak.saml.processing.core.parsers.saml.metadata;

import javax.xml.namespace.QName;

import org.keycloak.saml.common.parsers.AbstractStaxParser;
import org.keycloak.saml.processing.core.parsers.util.QNameEnumLookup;

/**
 * SAML 元数据 STAX 解析器抽象基类。
 * <p>通过 {@link SAMLMetadataQNames} 枚举将 XML 子标签映射为解析分支。</p>
 *
 * @param <T> 解析结果对应的 Java 类型
 * @author mhajas
 */
abstract public class AbstractStaxSamlMetadataParser<T> extends AbstractStaxParser<T, SAMLMetadataQNames> {

    /** QName 到 {@link SAMLMetadataQNames} 的查找表。 */
    protected static final QNameEnumLookup<SAMLMetadataQNames> LOOKUP = new QNameEnumLookup(SAMLMetadataQNames.values());


    /** 构造并指定期望的起始元数据元素。 */
    public AbstractStaxSamlMetadataParser(SAMLMetadataQNames expectedStartElement) {
        super(expectedStartElement.getQName(), SAMLMetadataQNames.UNKNOWN_ELEMENT);
    }

    /** 将 XML 元素 QName 映射为枚举常量。 */
    @Override
    protected SAMLMetadataQNames getElementFromName(QName name) {
        return LOOKUP.from(name);
    }
}
