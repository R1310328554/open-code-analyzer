package org.keycloak.saml.processing.core.parsers.saml.assertion;

import java.math.BigInteger;
import java.net.URI;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.StartElement;

import org.keycloak.dom.saml.v2.assertion.ProxyRestrictionType;
import org.keycloak.saml.common.exceptions.ParsingException;
import org.keycloak.saml.common.util.StaxParserUtil;

/**
 * 解析 SAML 条件中的 {@code ProxyRestriction} 元素。
 * <p>读取 Count 属性及允许的 Audience 列表，限制断言代理次数。</p>
 *
 * @author Patric Vormstein
 * @since 21.03.2018
 */
public class SAMLProxyRestrictionParser extends AbstractStaxSamlAssertionParser<ProxyRestrictionType> {

    /** 单例实例。 */
    private static final SAMLProxyRestrictionParser INSTANCE = new SAMLProxyRestrictionParser();

    /** 构造并绑定 PROXY_RESTRICTION 根元素。 */
    public SAMLProxyRestrictionParser() {
        super(SAMLAssertionQNames.PROXY_RESTRICTION);
    }

    /** @return 解析器单例 */
    public static SAMLProxyRestrictionParser getInstance() {
        return INSTANCE;
    }

    /** 创建代理限制对象并读取 Count 属性。 */
    @Override
    protected ProxyRestrictionType instantiateElement(XMLEventReader xmlEventReader, StartElement element) throws ParsingException {
        ProxyRestrictionType proxyRestriction = new ProxyRestrictionType();
        Integer count = StaxParserUtil.getIntegerAttributeValue(element, SAMLAssertionQNames.ATTR_COUNT);

        if (count != null) {
            proxyRestriction.setCount(BigInteger.valueOf(count));
        }

        return proxyRestriction;
    }

    /** 解析 Audience 子元素。 */
    @Override
    protected void processSubElement(XMLEventReader xmlEventReader, ProxyRestrictionType target, SAMLAssertionQNames element, StartElement elementDetail) throws ParsingException {
        switch (element) {
            case AUDIENCE:
                StaxParserUtil.advance(xmlEventReader);
                String audienceValue = StaxParserUtil.getElementText(xmlEventReader);
                target.addAudience(URI.create(audienceValue));
                break;

            default:
                throw LOGGER.parserUnknownTag(StaxParserUtil.getElementName(elementDetail), elementDetail.getLocation());
        }
    }
}
