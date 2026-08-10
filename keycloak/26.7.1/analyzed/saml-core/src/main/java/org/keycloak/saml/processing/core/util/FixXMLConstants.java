package org.keycloak.saml.processing.core.util;

/**
 * 从 XMLConstants 复制的常量，用于规避 IntelliJ 兼容性问题。
 * <p>参见 https://issues.redhat.com/browse/KEYCLOAK-19403</p>
 */
public class FixXMLConstants {

    /** 控制外部 DTD 访问的属性名。 */
    public static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";

    /** 控制外部 Schema 访问的属性名。 */
    public static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";

    /** 控制外部样式表访问的属性名。 */
    public static final String ACCESS_EXTERNAL_STYLESHEET = "http://javax.xml.XMLConstants/property/accessExternalStylesheet";

}
