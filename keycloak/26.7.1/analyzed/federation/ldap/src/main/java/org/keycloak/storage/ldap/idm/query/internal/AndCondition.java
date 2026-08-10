/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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
 * LDAP 逻辑与（{@code &}）复合条件，依次拼接各子条件的 filter 片段。
 *
 * @author rmartinc
 */
public class AndCondition implements Condition {

    private final Condition[] innerConditions;

    /** 由多个子条件构成 AND 过滤器。 */
    public AndCondition(Condition... innerConditions) {
        this.innerConditions = innerConditions;
    }

    @Override
    public String getParameterName() {
        return null;
    }

    @Override
    public void setParameterName(String parameterName) {
    }

    @Override
    public void updateParameterName(String modelParamName, String ldapParamName) {
        for (Condition innerCondition : innerConditions) {
            innerCondition.updateParameterName(modelParamName, ldapParamName);
        }
    }

    @Override
    public void applyCondition(StringBuilder filter) {
        filter.append("(&");

        for (Condition innerCondition : innerConditions) {
            innerCondition.applyCondition(filter);
        }

        filter.append(")");
    }

    @Override
    public void setBinary(boolean binary) {
    }

    @Override
    public boolean isBinary() {
        return false;
    }
}
