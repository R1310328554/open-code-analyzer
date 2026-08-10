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
package org.keycloak.saml.processing.core.constants;

/**
 * SAML 属性相关常量定义。
 * Constants for attributes
 *
 * @author Anil.Saldhana@redhat.com
 * @since Aug 31, 2009
 */
public interface AttributeConstants {

    /** 角色集合属性名。 */
    String ROLES = "roles";

    /**
     * SAML2 AttributeStatement 中表示角色的默认标识符。
     *
     * Default identifier in the saml2 attribute statements to indicate role *
     */
    String ROLE_IDENTIFIER_ASSERTION = "Role";
}