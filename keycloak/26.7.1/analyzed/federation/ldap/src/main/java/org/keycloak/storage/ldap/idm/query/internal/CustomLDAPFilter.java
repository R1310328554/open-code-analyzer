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

import org.keycloak.storage.ldap.idm.query.Condition;

/**
 * 自定义 LDAP 过滤器条件，将调用方提供的原始过滤器字符串直接拼入查询。
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
class CustomLDAPFilter implements Condition {

    /** 预定义的 LDAP 过滤器片段。 */
    private final String customFilter;

    /**
     * @param customFilter 原始 LDAP 过滤器字符串
     */
    public CustomLDAPFilter(String customFilter) {
        this.customFilter = customFilter;
    }

    /** {@inheritDoc} 自定义过滤器不绑定单一属性，返回 {@code null}。 */
    @Override
    public String getParameterName() {
        return null;
    }

    /** {@inheritDoc} 无操作。 */
    @Override
    public void setParameterName(String parameterName) {
    }

    /** {@inheritDoc} 自定义过滤器不参与模型到 LDAP 属性名的映射。 */
    @Override
    public void updateParameterName(String modelParamName, String ldapParamName) {

    }

    /** {@inheritDoc} 直接追加自定义过滤器字符串。 */
    @Override
    public void applyCondition(StringBuilder filter) {
        filter.append(customFilter);
    }

    /** {@inheritDoc} 无操作。 */
    @Override
    public void setBinary(boolean binary) {
    }

    /** {@inheritDoc} 始终为非二进制模式。 */
    @Override
    public boolean isBinary() {
        return false;
    }
}
