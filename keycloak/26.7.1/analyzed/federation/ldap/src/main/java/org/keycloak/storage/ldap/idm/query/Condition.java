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

package org.keycloak.storage.ldap.idm.query;

/**
 * LDAP 过滤器条件：描述查询参数如何编码为 LDAP search filter 片段。
 *
 * @author Pedro Igor
 */
public interface Condition {

    String getParameterName();
    void setParameterName(String parameterName);

    /**
     * 将模型参数名（如 firstName）重写为 LDAP 属性名（如 givenName）；复合条件应递归更新子条件。
     *
     * Will change the parameter name if it is "modelParamName" to "ldapParamName" . Implementation can apply this to subconditions as well.
     * It is used to update LDAP queries, which were created with model parameter name ( for example "firstName" ) and rewrite them to use real
     * LDAP mapped attribute (for example "givenName" )
     */
    void updateParameterName(String modelParamName, String ldapParamName);

    void applyCondition(StringBuilder filter);

    void setBinary(boolean binary);

    boolean isBinary();

    default String toFilter() {
        StringBuilder sb = new StringBuilder();
        applyCondition(sb);
        return sb.toString();
    }
}