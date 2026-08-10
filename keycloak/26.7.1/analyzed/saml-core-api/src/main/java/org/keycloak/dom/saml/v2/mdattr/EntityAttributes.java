package org.keycloak.dom.saml.v2.mdattr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.keycloak.dom.saml.v2.assertion.AssertionType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;

/**
 *
 * *
 * <p>
 * Java class for EntityAttributes complex type.
 * SAML 元数据实体属性扩展：可包含 SAML 属性或嵌套断言。

 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
* 	&lt;element name="EntityAttributes" type="mdattr:EntityAttributesType"/>
* 	&lt;complexType name="EntityAttributesType">
* 		&lt;choice maxOccurs="unbounded">
* 			&lt;element ref="saml:Attribute"/>
* 			&lt;element ref="saml:Assertion"/>
* 		&lt;/sequence>
* 	&lt;/complexType>
 *
 * </pre>
 *
 */

public class EntityAttributes implements Serializable {

    protected List<AttributeType> attribute = new ArrayList<>();
    protected List<AssertionType> assertion = new ArrayList<>();

    /** 获取属性列表。 */
    public List<AttributeType> getAttribute() {
        return attribute;
    }

    /** 添加一条属性。 */
    public void addAttribute(AttributeType attributeType) {
        attribute.add(attributeType);
    }

    /** 移除一条属性。 */
    public void removeAttribute(AttributeType attributeType) {
        attribute.remove(attributeType);
    }

    /** 获取嵌套断言列表。 */
    public List<AssertionType> getAssertion() {
        return assertion;
    }

    /** 添加一条嵌套断言。 */
    public void addAssertion(AssertionType assertionType) {
        assertion.add(assertionType);
    }

    /** 移除一条嵌套断言。 */
    public void removeAssertion(AssertionType assertionType) {
        assertion.remove(assertionType);
    }

}
