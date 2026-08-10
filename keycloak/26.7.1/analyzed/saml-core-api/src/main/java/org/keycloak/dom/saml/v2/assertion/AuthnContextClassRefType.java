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
package org.keycloak.dom.saml.v2.assertion;

import java.io.Serializable;
import java.net.URI;

/**
 * SAML 2.0 认证上下文类引用（AuthnContextClassRef）类型，以 URI 标识认证上下文类别。
 *
 * Type that represents an AuthnContextClassRef
 *
 * @author Anil.Saldhana@redhat.com
 * @since Nov 24, 2010
 */
public class AuthnContextClassRefType implements URIType, Serializable {

    private final URI value;

    /** 以 URI 值构造认证上下文类引用。 */
    public AuthnContextClassRefType(URI value) {
        this.value = value;
    }

    /** 获取认证上下文类 URI 值。 */
    public URI getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "AuthnContextClassRefType [value=" + value + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AuthnContextClassRefType other = (AuthnContextClassRefType) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
}