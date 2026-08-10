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

package org.keycloak.adapters.saml;

import java.io.Serializable;
import java.util.Set;
import javax.xml.datatype.XMLGregorianCalendar;

import org.keycloak.adapters.spi.KeycloakAccount;

/**
 * SAML 认证会话，封装主体、角色、会话索引及过期时间。
 *
 * <p>实现 {@link KeycloakAccount} 与 {@link Serializable}，可存入 HTTP 会话或适配器会话存储。</p>
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SamlSession implements Serializable, KeycloakAccount {
    /** SAML 主体（含断言属性）。 */
    private SamlPrincipal principal;
    /** 映射后的应用角色集合。 */
    private Set<String> roles;
    /** IdP 分配的会话索引，用于单点登出。 */
    private String sessionIndex;
    /** 会话不得晚于此时间失效（SAML SessionNotOnOrAfter）。 */
    private XMLGregorianCalendar sessionNotOnOrAfter;

    /** 无参构造，供序列化框架使用。 */
    public SamlSession() {
    }

    /**
     * 创建完整 SAML 会话。
     *
     * @param principal SAML 主体
     * @param roles 角色集合
     * @param sessionIndex IdP 会话索引
     * @param sessionNotOnOrAfter 会话最晚有效时间
     */
    public SamlSession(SamlPrincipal principal, Set<String> roles, String sessionIndex, XMLGregorianCalendar sessionNotOnOrAfter) {
        this.principal = principal;
        this.roles = roles;
        this.sessionIndex = sessionIndex;
        this.sessionNotOnOrAfter = sessionNotOnOrAfter;
    }

    /** @return SAML 主体 */
    public SamlPrincipal getPrincipal() {
        return principal;
    }

    /** @return 应用角色集合 */
    public Set<String> getRoles() {
        return roles;
    }

    /** @return IdP 会话索引 */
    public String getSessionIndex() {
        return sessionIndex;
    }

    /** @return 会话最晚有效时间 */
    public XMLGregorianCalendar getSessionNotOnOrAfter() {
        return sessionNotOnOrAfter;
    }

    /** 基于主体、角色与会话索引判断相等性（不含过期时间）。 */
    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (!(other instanceof SamlSession))
            return false;

        SamlSession otherSession = (SamlSession) other;

        return (this.principal != null ? this.principal.equals(otherSession.principal) : otherSession.principal == null) &&
                (this.roles != null ? this.roles.equals(otherSession.roles) : otherSession.roles == null) &&
                (this.sessionIndex != null ? this.sessionIndex.equals(otherSession.sessionIndex) : otherSession.sessionIndex == null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.principal == null ? 0 : this.principal.hashCode());
        result = prime * result + (this.roles == null ? 0 : this.roles.hashCode());
        result = prime * result + (this.sessionIndex == null ? 0 : this.sessionIndex.hashCode());
        return result;
    }
}
