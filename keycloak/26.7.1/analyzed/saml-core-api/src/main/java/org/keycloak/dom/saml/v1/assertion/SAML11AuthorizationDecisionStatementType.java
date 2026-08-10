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
package org.keycloak.dom.saml.v1.assertion;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <complexType name="AuthorizationDecisionStatementType">
 * SAML 1.1 授权决策语句 DOM 类型：对指定资源与动作给出 Permit/Deny/Indeterminate 决策，可附证据断言。
 <complexContent> <extension
 * base="saml:SubjectStatementAbstractType">
 * <sequence> <element ref="saml:Action" maxOccurs="unbounded"/> <element ref="saml:Evidence" minOccurs="0"/>
 *
 * </sequence> <attribute name="Resource" type="anyURI" use="required"/> <attribute name="Decision"
 * type="saml:DecisionType"
 * use="required"/> </extension> </complexContent> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AuthorizationDecisionStatementType extends SAML11SubjectStatementType {

    /** 待评估的动作列表。 */
    protected List<SAML11ActionType> actions = new ArrayList<>();

    /** 支撑决策的可选证据。 */
    protected SAML11EvidenceType evidence;

    /** 被授权评估的资源 URI。 */
    protected URI resource;

    /** 对该资源与动作的授权决策。 */
    protected SAML11DecisionType decision;

    /** 构造授权决策语句，指定资源与决策结果。 */
    public SAML11AuthorizationDecisionStatementType(URI resource, SAML11DecisionType decision) {
        this.resource = resource;
        this.decision = decision;
    }

    /** 返回被评估资源 URI。 */
    public URI getResource() {
        return resource;
    }

    /** 返回授权决策。 */
    public SAML11DecisionType getDecision() {
        return decision;
    }

    /** 添加一条待评估动作。 */
    public void addAction(SAML11ActionType action) {
        this.actions.add(action);
    }

    /** 移除指定动作。 */
    public boolean removeAction(SAML11ActionType action) {
        return this.actions.remove(action);
    }

    /** 返回不可修改的动作列表。 */
    public List<SAML11ActionType> getActions() {
        return Collections.unmodifiableList(actions);
    }

    /** 返回关联证据。 */
    public SAML11EvidenceType getEvidence() {
        return evidence;
    }

    /** 设置支撑决策的证据。 */
    public void setEvidence(SAML11EvidenceType evidence) {
        this.evidence = evidence;
    }
}