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
package org.keycloak.dom.saml.v1.protocol;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.keycloak.dom.saml.v1.assertion.SAML11ActionType;
import org.keycloak.dom.saml.v1.assertion.SAML11EvidenceType;

/**
 * SAML 1.1 授权决策查询（AuthorizationDecisionQuery）：请求对指定主体在资源上的操作授权决策。
 *
 * <complexType name="AuthorizationDecisionQueryType"> <complexContent> <extension
 * base="samlp:SubjectQueryAbstractType">
 * <sequence>
 *
 * <element ref="saml:Action" maxOccurs="unbounded"/> <element ref="saml:Evidence" minOccurs="0"/> </sequence>
 * <attribute
 * name="Resource" type="anyURI" use="required"/> </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AuthorizationDecisionQueryType extends SAML11SubjectQueryAbstractType {

    /** 待决策的操作（Action）列表。 */
    protected List<SAML11ActionType> action = new ArrayList<>();

    /** 可选的支持证据（Evidence）。 */
    protected SAML11EvidenceType evidence;

    /** 目标资源 URI（必填）。 */
    protected URI resource;

    /** 返回资源 URI。 */
    public URI getResource() {
        return resource;
    }

    /** 设置资源 URI。 */
    public void setResource(URI resource) {
        this.resource = resource;
    }

    /** 返回证据。 */
    public SAML11EvidenceType getEvidence() {
        return evidence;
    }

    /** 设置证据。 */
    public void setEvidence(SAML11EvidenceType evidence) {
        this.evidence = evidence;
    }

    /** 添加操作。 */
    public void add(SAML11ActionType sadt) {
        this.action.add(sadt);
    }

    /** 移除操作。 */
    public boolean remove(SAML11ActionType sadt) {
        return this.action.remove(sadt);
    }

    /** 返回不可修改的操作列表。 */
    public List<SAML11ActionType> get() {
        return Collections.unmodifiableList(action);
    }
}
