package org.keycloak.saml.processing.core.parsers.saml.metadata;

import javax.xml.namespace.QName;

import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.processing.core.parsers.saml.assertion.SAMLAssertionQNames;
import org.keycloak.saml.processing.core.parsers.saml.xmldsig.XmlDSigQNames;
import org.keycloak.saml.processing.core.parsers.util.HasQName;


/**
 * SAML 元数据解析器使用的 XML 元素与属性 QName 枚举。
 * <p>涵盖元数据核心元素、MDUI 扩展、属性名及跨命名空间子元素映射。</p>
 *
 * @author mhajas
 */
public enum SAMLMetadataQNames implements HasQName {
    /** 附加元数据位置。 */
    ADDITIONAL_METADATA_LOCATION("AdditionalMetadataLocation"),
    /** 附属成员。 */
    AFFILIATE_MEMBER("AffiliateMember"),
    /** 附属关系描述符。 */
    AFFILIATION_DESCRIPTOR("AffiliationDescriptor"),
    /** Artifact 解析服务端点。 */
    ARTIFACT_RESOLUTION_SERVICE("ArtifactResolutionService"),
    /** 断言消费服务端点。 */
    ASSERTION_CONSUMER_SERVICE("AssertionConsumerService"),
    /** 断言 ID 请求服务端点。 */
    ASSERTION_ID_REQUEST_SERVICE("AssertionIDRequestService"),
    /** 属性权威描述符。 */
    ATTRIBUTE_AUTHORITY_DESCRIPTOR("AttributeAuthorityDescriptor"),
    /** 属性消费服务。 */
    ATTRIBUTE_CONSUMING_SERVICE("AttributeConsumingService"),
    /** 属性配置文件。 */
    ATTRIBUTE_PROFILE("AttributeProfile"),
    /** 属性服务端点。 */
    ATTRIBUTE_SERVICE("AttributeService"),
    /** 属性值。 */
    ATTRIBUTE_VALUE("AttributeValue"),
    /** 认证权威描述符。 */
    AUTHN_AUTHORITY_DESCRIPTOR("AuthnAuthorityDescriptor"),
    /** 认证查询服务端点。 */
    AUTHN_QUERY_SERVICE("AuthnQueryService"),
    /** 授权服务端点。 */
    AUTHZ_SERVICE("AuthzService"),
    /** 公司名称。 */
    COMPANY("Company"),
    /** 联系人。 */
    CONTACT_PERSON("ContactPerson"),
    /** 电子邮件地址。 */
    EMAIL_ADDRESS("EmailAddress"),
    /** 加密方法。 */
    ENCRYPTION_METHOD("EncryptionMethod"),
    /** 实体集合描述符。 */
    ENTITIES_DESCRIPTOR("EntitiesDescriptor"),
    /** 实体描述符。 */
    ENTITY_DESCRIPTOR("EntityDescriptor"),
    /** 扩展容器。 */
    EXTENSIONS("Extensions"),
    /** 名字。 */
    GIVEN_NAME("GivenName"),
    /** IdP SSO 描述符。 */
    IDP_SSO_DESCRIPTOR("IDPSSODescriptor"),
    /** 密钥描述符。 */
    KEY_DESCRIPTOR("KeyDescriptor"),
    /** NameID 管理服务端点。 */
    MANAGE_NAMEID_SERVICE("ManageNameIDService"),
    /** NameID 格式。 */
    NAMEID_FORMAT("NameIDFormat"),
    /** NameID 映射服务端点。 */
    NAMEID_MAPPING_SERVICE("NameIDMappingService"),
    /** 组织显示名称。 */
    ORGANIZATION_DISPLAY_NAME("OrganizationDisplayName"),
    /** 组织名称。 */
    ORGANIZATION_NAME("OrganizationName"),
    /** 组织信息。 */
    ORGANIZATION("Organization"),
    /** 组织 URL。 */
    ORGANIZATION_URL("OrganizationURL"),
    /** 组织 URL（非标准别名，KEYCLOAK-4040）。 */
    ORGANIZATION_URL_ALT("OrganizationUrl"),    // non-standard: KEYCLOAK-4040,
    /** 策略决策点描述符。 */
    PDP_DESCRIPTOR("PDPDescriptor"),
    /** 请求属性。 */
    REQUESTED_ATTRIBUTE("RequestedAttribute"),
    /** 角色描述符。 */
    ROLE_DESCRIPTOR("RoleDescriptor"),
    /** 服务描述。 */
    SERVICE_DESCRIPTION("ServiceDescription"),
    /** 服务名称。 */
    SERVICE_NAME("ServiceName"),
    /** 单点登出服务端点。 */
    SINGLE_LOGOUT_SERVICE("SingleLogoutService"),
    /** 单点登录服务端点。 */
    SINGLE_SIGNON_SERVICE("SingleSignOnService"),
    /** SP SSO 描述符。 */
    SP_SSO_DESCRIPTOR("SPSSODescriptor"),
    /** 姓氏。 */
    SURNAME("SurName"),
    /** 电话号码。 */
    TELEPHONE_NUMBER("TelephoneNumber"),

    // MDUI 元素
    /** UI 描述文本。 */
    DESCRIPTION(JBossSAMLURIConstants.METADATA_UI, "Description"),
    /** UI 显示名称。 */
    DISPLAY_NAME(JBossSAMLURIConstants.METADATA_UI, "DisplayName"),
    /** UI 信息 URL。 */
    INFORMATION_URL(JBossSAMLURIConstants.METADATA_UI, "InformationURL"),
    /** UI 关键词。 */
    KEYWORDS(JBossSAMLURIConstants.METADATA_UI, "Keywords"),
    /** UI 徽标。 */
    LOGO(JBossSAMLURIConstants.METADATA_UI, "Logo"),
    /** UI 隐私声明 URL。 */
    PRIVACY_STATEMENT_URL(JBossSAMLURIConstants.METADATA_UI, "PrivacyStatementURL"),
    /** UI 信息容器。 */
    UIINFO(JBossSAMLURIConstants.METADATA_UI, "UIInfo"),

    // 属性名
    /** 实体 ID 属性。 */
    ATTR_ENTITY_ID(null, "entityID"),
    /** ID 属性。 */
    ATTR_ID(null, "ID"),
    /** 有效期截止属性。 */
    ATTR_VALID_UNTIL(null, "validUntil"),
    /** 缓存时长属性。 */
    ATTR_CACHE_DURATION(null, "cacheDuration"),
    /** 协议支持枚举属性。 */
    ATTR_PROTOCOL_SUPPORT_ENUMERATION(null, "protocolSupportEnumeration"),
    /** 密钥用途属性。 */
    ATTR_USE(null, "use"),
    /** 算法属性。 */
    ATTR_ALGORITHM(null, "Algorithm"),
    /** 语言属性。 */
    ATTR_LANG(JBossSAMLURIConstants.XML, "lang"),
    /** 联系人类型属性。 */
    ATTR_CONTACT_TYPE(null, "contactType"),
    /** 认证请求是否签名属性。 */
    ATTR_AUTHN_REQUESTS_SIGNED(null, "AuthnRequestsSigned"),
    /** 是否要求断言签名属性。 */
    ATTR_WANT_ASSERTIONS_SIGNED(null, "WantAssertionsSigned"),
    /** 是否要求认证请求签名属性。 */
    ATTR_WANT_AUTHN_REQUESTS_SIGNED(null, "WantAuthnRequestsSigned"),
    /** 绑定协议属性。 */
    ATTR_BINDING(null, "Binding"),
    /** 端点位置属性。 */
    ATTR_LOCATION(null, "Location"),
    /** 是否默认端点属性。 */
    ATTR_IS_DEFAULT(null, "isDefault"),
    /** 端点索引属性。 */
    ATTR_INDEX(null, "index"),
    /** 响应位置属性。 */
    ATTR_RESPONSE_LOCATION(null, "ResponseLocation"),
    /** 友好名称属性。 */
    ATTR_FRIENDLY_NAME(null, "FriendlyName"),
    /** 是否必需属性。 */
    ATTR_IS_REQUIRED(null, "isRequired"),
    /** 名称属性。 */
    ATTR_NAME(null, "Name"),
    /** 名称格式属性。 */
    ATTR_NAME_FORMAT(null, "NameFormat"),
    /** 宽度属性。 */
    ATTR_WIDTH(null, "width"),
    /** 高度属性。 */
    ATTR_HEIGHT(null, "height"),
    // 可作为本命名空间元素直接子元素的跨命名空间元素
    /** XML 数字签名元素。 */
    SIGNATURE(XmlDSigQNames.SIGNATURE),
    /** 密钥信息元素。 */
    KEY_INFO(XmlDSigQNames.KEY_INFO),
    /** 密钥长度元素。 */
    KEY_SIZE(JBossSAMLURIConstants.XMLENC_NSURI, "KeySize"),
    /** OAEP 参数元素。 */
    OAEP_PARAMS(JBossSAMLURIConstants.XMLENC_NSURI, "OAEPparams"),
    /** X500 编码属性。 */
    ATTR_X500_ENCODING(JBossSAMLURIConstants.X500_NSURI, "Encoding"),
    /** 断言属性元素。 */
    ATTRIBUTE(SAMLAssertionQNames.ATTRIBUTE),
    /** 断言元素。 */
    ASSERTION(SAMLAssertionQNames.ASSERTION),
    /** 实体属性扩展元素。 */
    ENTITY_ATTRIBUTES(JBossSAMLURIConstants.METADATA_ENTITY_ATTRIBUTES_NSURI, "EntityAttributes"),

    /** 未知元素占位符。 */
    UNKNOWN_ELEMENT("");

    /** 对应的 XML QName。 */
    private final QName qName;

    /** 使用元数据命名空间构造 QName。 */
    SAMLMetadataQNames(String localName) {
        this.qName = new QName(JBossSAMLURIConstants.METADATA_NSURI.get(), localName);
    }

    /** 从已有 {@link HasQName} 源复制 QName。 */
    SAMLMetadataQNames(HasQName source) {
        this.qName = source.getQName();
    }

    /** 使用指定命名空间 URI 构造 QName。 */
    SAMLMetadataQNames(JBossSAMLURIConstants nsUri, String localName) {
        this.qName = new QName(nsUri == null ? null : nsUri.get(), localName);
    }

    /** @return 枚举对应的 QName */
    @Override
    public QName getQName() {
        return qName;
    }

    /**
     * @param prefix 命名空间前缀
     * @return 带前缀的 QName
     */
    public QName getQName(String prefix) {
        return new QName(this.qName.getNamespaceURI(), this.qName.getLocalPart(), prefix);
    }
}
