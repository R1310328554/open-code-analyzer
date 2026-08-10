/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.keycloak.saml.processing.core.saml.v2.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.keycloak.dom.saml.v2.assertion.AttributeStatementType;
import org.keycloak.dom.saml.v2.assertion.AttributeStatementType.ASTChoiceType;
import org.keycloak.dom.saml.v2.assertion.AttributeType;
import org.keycloak.dom.saml.v2.assertion.AuthnContextClassRefType;
import org.keycloak.dom.saml.v2.assertion.AuthnContextType;
import org.keycloak.dom.saml.v2.assertion.AuthnStatementType;
import org.keycloak.dom.saml.v2.assertion.StatementAbstractType;
import org.keycloak.saml.common.constants.JBossSAMLURIConstants;
import org.keycloak.saml.common.util.StringUtil;
import org.keycloak.saml.processing.core.constants.AttributeConstants;
import org.keycloak.saml.processing.core.saml.v2.constants.X500SAMLProfileConstants;

/**
 * SAML 2.0 语句（Statement）构造与转换工具。
 * <p>支持认证语句、属性语句的创建，以及属性集合与 Map 的互转。</p>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 31, 2009
 */
public class StatementUtil {

    /** X500 编码属性的 QName。 */
    public static final QName X500_QNAME = new QName(JBossSAMLURIConstants.X500_NSURI.get(), "Encoding",
            JBossSAMLURIConstants.X500_PREFIX.get());

    /**
     * 根据签发时刻与认证上下文类引用创建 {@link AuthnStatementType}。
     *
     * @param instant 签发时刻，类型为 {@link XMLGregorianCalendar}
     * @param authnContextClassRefValue 认证上下文类引用 URI
     *
     * @return {@link AuthnStatementType} 认证语句
     */
    public static AuthnStatementType createAuthnStatement(XMLGregorianCalendar instant, String authnContextClassRefValue) {
        AuthnStatementType authnStatement = new AuthnStatementType(instant);

        AuthnContextType authnContext = new AuthnContextType();
        AuthnContextClassRefType authnContextClassRef = new AuthnContextClassRefType(URI.create(authnContextClassRefValue));

        AuthnContextType.AuthnContextTypeSequence sequence = new AuthnContextType.AuthnContextTypeSequence();
        sequence.setClassRef(authnContextClassRef);
        authnContext.setSequence(sequence);

        authnStatement.setAuthnContext(authnContext);

        return authnStatement;
    }

    /**
     * 根据属性映射创建属性语句。
     *
     * @param attributes 属性键值映射，键来自 {@link AttributeConstants}
     *
     * @return 属性语句，无属性时可能为 {@code null}
     */
    public static AttributeStatementType createAttributeStatement(Map<String, Object> attributes) {
        AttributeStatementType attrStatement = null;

        int i = 0;

        for (var entry : attributes.entrySet()) {
            String key = entry.getKey();
            if (i == 0) {
                // 处理 SAML2 X500 Profile
                attrStatement = new AttributeStatementType();
                i++;
            }

            // 若属性值为角色集合，则逐个角色添加为属性
            if (AttributeConstants.ROLES.equalsIgnoreCase(key)) {
                Object value = entry.getValue();
                if (value instanceof Collection<?>) {
                    Collection<?> roles = (Collection<?>) value;
                    attrStatement = createAttributeStatement(new ArrayList(roles));
                }
            } else {
                AttributeType att;
                Object value = entry.getValue();

                String uri = X500SAMLProfileConstants.getOID(key);
                if (StringUtil.isNotNull(uri)) {
                    att = getX500Attribute(uri);
                    att.setFriendlyName(key);
                } else {
                    att = new AttributeType(key);
                    att.setFriendlyName(key);
                    att.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_URI.get());
                }

                if (Collection.class.isInstance(value)) {
                    Collection collection = (Collection) value;
                    Iterator iterator = collection.iterator();

                    while (iterator.hasNext()) {
                        att.addAttributeValue(iterator.next());
                    }
                } else if (String.class.isInstance(value)) {
                    att.addAttributeValue(value);
                } else {
                    throw new RuntimeException("Unsupported attribute value [" + value + "]. Values must be a string, even if using a Collection.");
                }

                attrStatement.addAttribute(new ASTChoiceType(att));
            }
        }
        return attrStatement;
    }

    /**
     * 根据角色列表创建属性语句（每个角色单独一条属性）。
     *
     * @param roles 角色名称列表
     *
     * @return 属性语句
     */
    public static AttributeStatementType createAttributeStatement(List<String> roles) {
        AttributeStatementType attrStatement = null;
        for (String role : roles) {
            if (attrStatement == null) {
                attrStatement = new AttributeStatementType();
            }
            AttributeType attr = new AttributeType(AttributeConstants.ROLE_IDENTIFIER_ASSERTION);
            attr.addAttributeValue(role);
            attrStatement.addAttribute(new ASTChoiceType(attr));
        }
        return attrStatement;
    }

    /**
     * 根据角色列表创建属性语句。
     *
     * @param roles 角色名称列表
     * @param multivalued 是否合并为单条多值属性
     *
     * @return 属性语句
     */
    public static AttributeStatementType createAttributeStatementForRoles(List<String> roles, boolean multivalued) {
        if (!multivalued) {
            return createAttributeStatement(roles);
        }
        AttributeStatementType attrStatement = new AttributeStatementType();
        AttributeType attr = new AttributeType(AttributeConstants.ROLE_IDENTIFIER_ASSERTION);
        for (String role : roles) {
            attr.addAttributeValue(role);
        }
        attrStatement.addAttribute(new ASTChoiceType(attr));
        return attrStatement;
    }

    /**
     * 根据单个键值对创建 {@link AttributeStatementType}。
     *
     * @param key 属性名
     * @param value 属性值
     *
     * @return 属性语句
     */
    public static AttributeStatementType createAttributeStatement(String key, String value) {
        AttributeStatementType attrStatement = new AttributeStatementType();
        AttributeType attr = new AttributeType(key);
        attr.addAttributeValue(value);
        attrStatement.addAttribute(new ASTChoiceType(attr));

        return attrStatement;
    }

    /** 将属性语句集合转换为 Map（键为友好名或属性名）。 */
    public static Map<String, Object> asMap(Set<AttributeStatementType> attributeStatementTypes) {
        Map<String, Object> attrMap = new HashMap<>();

        if (attributeStatementTypes != null && !attributeStatementTypes.isEmpty()) {
            attrMap = new HashMap<>();

            for (StatementAbstractType statement : attributeStatementTypes) {
                if (statement instanceof AttributeStatementType) {
                    AttributeStatementType attrStat = (AttributeStatementType) statement;
                    List<ASTChoiceType> attrs = attrStat.getAttributes();
                    for (ASTChoiceType attrChoice : attrs) {
                        AttributeType attr = attrChoice.getAttribute();
                        String attributeName = attr.getFriendlyName();

                        if (attributeName == null) {
                            attributeName = attr.getName();
                        }

                        List<Object> values = attr.getAttributeValue();

                        if (values != null) {
                            if (values.size() == 1) {
                                attrMap.put(attributeName, values.get(0));
                            } else {
                                attrMap.put(attributeName, values);
                            }
                        }
                    }
                }
            }
        }

        return attrMap;
    }

    /** 创建带 X500 LDAP 编码标记的属性类型。 */
    private static AttributeType getX500Attribute(String name) {
        AttributeType att = new AttributeType(name);
        att.getOtherAttributes().put(X500_QNAME, "LDAP");

        att.setNameFormat(JBossSAMLURIConstants.ATTRIBUTE_FORMAT_URI.get());
        return att;
    }
}
