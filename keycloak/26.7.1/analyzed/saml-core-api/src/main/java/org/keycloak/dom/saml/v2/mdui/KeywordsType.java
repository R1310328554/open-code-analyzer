package org.keycloak.dom.saml.v2.mdui;

import java.util.List;

/**
 * <p>
 * Java class for localizedURIType complex type.
 * SAML MDUI 关键词类型：按 xml:lang 本地化的一组搜索关键词字符串。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 *  &lt;complexType name="KeywordsType">
 *   &lt;simpleContent>
 *     &lt;extension base="mdui:listOfStrings">
 *       &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang  use="required""/>
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * &lt;simpleType name="listOfStrings">
 *   &lt;list itemType="string"/>
 * &lt;/simpleType>
 * </pre>
 */
public class KeywordsType {

    protected List<String> values;
    protected String lang;

    /** 以语言标签构造关键词列表。 */
    public KeywordsType(String lang) {
        this.lang = lang;
    }

    /** 获取关键词字符串列表。 */
    public List<String> getValues() {
        return values;
    }

    /** 设置关键词字符串列表。 */
    public void setValues(List<String> values) {
        this.values = values;
    }

    /** 获取 xml:lang 语言标签。 */
    public String getLang() {
        return lang;
    }



}