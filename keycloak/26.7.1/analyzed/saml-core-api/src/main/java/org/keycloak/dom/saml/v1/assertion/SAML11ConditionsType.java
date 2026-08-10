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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.keycloak.dom.saml.common.CommonConditionsType;

/**
 * <complexType name="ConditionsType">
 * SAML 1.1 条件集合（Conditions）DOM 类型：聚合受众限制、禁止缓存及自定义条件，并可指定 NotBefore/NotOnOrAfter。
 <choice minOccurs="0" maxOccurs="unbounded"> <element
 * ref="saml:AudienceRestrictionCondition"/> <element ref="saml:DoNotCacheCondition"/> <element ref="saml:Condition"/>
 * </choice>
 * <attribute name="NotBefore" type="dateTime" use="optional"/> <attribute name="NotOnOrAfter" type="dateTime"
 * use="optional"/>
 * </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11ConditionsType extends CommonConditionsType {

    /** 附加条件列表（受众限制、DoNotCache 等）。 */
    public List<SAML11ConditionAbstractType> conditions = new ArrayList<>();

    /** 添加一条条件。 */
    public void add(SAML11ConditionAbstractType condition) {
        this.conditions.add(condition);
    }

    /** 批量添加条件。 */
    public void addAll(List<SAML11ConditionAbstractType> theConditions) {
        this.conditions.addAll(theConditions);
    }

    /** 移除指定条件。 */
    public boolean remove(SAML11ConditionAbstractType condition) {
        return this.conditions.remove(condition);
    }

    /** 返回不可修改的条件列表。 */
    public List<SAML11ConditionAbstractType> get() {
        return Collections.unmodifiableList(conditions);
    }
}