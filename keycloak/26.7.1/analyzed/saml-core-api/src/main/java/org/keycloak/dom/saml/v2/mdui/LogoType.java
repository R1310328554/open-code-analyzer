package org.keycloak.dom.saml.v2.mdui;

import java.net.URI;

/**
 * <p>
 * Java class for localizedURIType complex type.
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 *  &lt;complexType name="LogoType">
 * SAML MDUI 徽标类型：携带 URI、宽高像素及可选 xml:lang 语言标签。

 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>anyURI">
 *       &lt;attribute name="height" type="positiveInteger" use="required""/>
 *       &lt;attribute name="width" type="positiveInteger" use="required""/>
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang "/>
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 */
public class LogoType {

    protected URI value;
    protected int height;
    protected int width;
    protected String lang;

    /** 以像素高度与宽度构造徽标。 */
    public LogoType(int height, int width) {
        this.height = height;
        this.width = width;
    }

    /**
     * 获取 徽标 URI 值 属性的值。
     *
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public URI getValue() {
        return value;
    }

    /**
     * 设置 徽标 URI 值 属性的值。
     *
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(URI value) {
        this.value = value;
    }

    /** 获取 xml:lang 语言标签。 */
    public String getLang() {
        return lang;
    }

    /** 设置 xml:lang 语言标签。 */
    public void setLang(String lang) {
        this.lang = lang;
    }

    /** 获取徽标高度（像素）。 */
    public int getHeight() {
        return height;
    }

    /** 获取徽标宽度（像素）。 */
    public int getWidth() {
        return width;
    }

}