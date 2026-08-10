/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.protocol.oid4vc.model;

/**
 * 可验证凭证类型（vct 等）的值对象包装。
 *
 * @author <a href="mailto:francis.pouatcha@adorsys.com">Francis Pouatcha</a>
 */
public class VerifiableCredentialType {
    /** 类型字符串值。 */
    private final String value;

    /** @param value 类型字符串，null 时返回 null */
    public static VerifiableCredentialType from(String value){
        return value == null? null : new VerifiableCredentialType(value);
    }
    /** @param value 类型字符串 */
    public VerifiableCredentialType(String value) {
        this.value = value;
    }

    /** @return 类型字符串 */
    public String getValue() {
        return value;
    }
}
