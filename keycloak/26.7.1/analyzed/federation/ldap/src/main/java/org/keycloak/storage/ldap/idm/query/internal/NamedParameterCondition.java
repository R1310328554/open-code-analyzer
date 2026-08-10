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

package org.keycloak.storage.ldap.idm.query.internal;

import java.util.Date;

import org.keycloak.storage.ldap.idm.query.Condition;
import org.keycloak.storage.ldap.idm.query.EscapeStrategy;
import org.keycloak.storage.ldap.idm.store.ldap.LDAPUtil;

/**
 * 带命名参数的 {@link Condition} 抽象基类，负责属性名映射与 LDAP 值转义。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class NamedParameterCondition implements Condition {

    /** LDAP 属性名（可随模型映射更新）。 */
    private String parameterName;
    /** 是否按 Octet-String 二进制语义转义。 */
    private boolean binary;

    /**
     * @param parameterName LDAP 属性名
     */
    public NamedParameterCondition(String parameterName) {
        this.parameterName = parameterName;
    }

    /** {@inheritDoc} */
    @Override
    public String getParameterName() {
        return parameterName;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }


    /**
     * {@inheritDoc}
     *
     * <p>当属性名与 {@code modelParamName} 忽略大小写匹配时，替换为 {@code ldapParamName}。</p>
     */
    @Override
    public void updateParameterName(String modelParamName, String ldapParamName) {
        if (parameterName.equalsIgnoreCase(modelParamName)) {
            this.parameterName = ldapParamName;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isBinary() {
        return binary;
    }

    /**
     * 将比较值转义为 LDAP 过滤器安全字符串。
     *
     * <p>{@link Date} 会先格式化为 LDAP 通用时间字符串。</p>
     */
    public String escapeValue(Object value) {
        if (Date.class.isInstance(value)) {
            value = LDAPUtil.formatDate((Date) value);
        }
        return new OctetStringEncoder(EscapeStrategy.DEFAULT).encode(value, isBinary());
    }
}
