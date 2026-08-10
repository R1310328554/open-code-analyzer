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

import org.keycloak.dom.saml.common.CommonAdviceType;

/**
 * SAML 1.1 Advice 类型，继承 {@link org.keycloak.dom.saml.common.CommonAdviceType}，可引用断言 ID、嵌套断言或携带扩展元素。
 *
 * <complexType name="AdviceType"> <choice minOccurs="0" maxOccurs="unbounded"> <element
 * ref="saml:AssertionIDReference"/>
 * <element ref="saml:Assertion"/> <any namespace="##other" processContents="lax"/> </choice> </complexType>
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 22, 2011
 */
public class SAML11AdviceType extends CommonAdviceType {

}