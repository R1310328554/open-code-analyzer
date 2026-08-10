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
package org.keycloak.dom.saml.v2.protocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.dom.saml.v2.assertion.ActionType;
import org.keycloak.dom.saml.v2.assertion.EvidenceType;

/**
 * <p>
 * Java class for AuthzDecisionQueryType complex type.
 * SAML 2.0 授权决策查询：请求对指定资源与动作的访问决策。
 *
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="AuthzDecisionQueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:oasis:names:tc:SAML:2.0:protocol}SubjectQueryAbstractType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Action" maxOccurs="unbounded"/>
 *         &lt;element ref="{urn:oasis:names:tc:SAML:2.0:assertion}Evidence" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Resource" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
public class AuthzDecisionQueryType extends SubjectQueryAbstractType {

    /** 请求授权的动作列表。 */
    protected List<ActionType> action = new ArrayList<>();

    /** 可选的决策证据。 */
    protected EvidenceType evidence;

    /** 待授权的资源 URI（必填）。 */
    protected URI resource;

    /** 构造授权决策查询。 */
    public AuthzDecisionQueryType(String id, XMLGregorianCalendar instant) {
        super(id, instant);
    }

    /**
     * 添加动作。
     *
     * Add an action
     *
     * @param act
     */
    public void addAction(ActionType act) {
        this.action.add(act);
    }

    /**
     * 移除动作。
     *
     * Remove an action
     *
     * @param act
     */
    public void removeAction(ActionType act) {
        this.action.remove(act);
    }

    /**
     * 获取 action 属性的值。
     *
     * Gets the value of the action property.
     */
    public List<ActionType> getAction() {
        return Collections.unmodifiableList(this.action);
    }

    /**
     * 获取 evidence 属性的值。
     *
     * Gets the value of the evidence property.
     *
     * @return possible object is {@link EvidenceType }
     */
    public EvidenceType getEvidence() {
        return evidence;
    }

    /**
     * 设置 evidence 属性的值。
     *
     * Sets the value of the evidence property.
     *
     * @param value allowed object is {@link EvidenceType }
     */
    public void setEvidence(EvidenceType value) {
        this.evidence = value;
    }

    /**
     * 获取 resource 属性的值。
     *
     * Gets the value of the resource property.
     *
     * @return possible object is {@link String }
     */
    public URI getResource() {
        return resource;
    }

    /**
     * 设置 resource 属性的值。
     *
     * Sets the value of the resource property.
     *
     * @param value allowed object is {@link String }
     */
    public void setResource(URI value) {
        this.resource = value;
    }
}
