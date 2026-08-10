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

package org.keycloak.models;

import java.io.Serializable;
import java.util.Comparator;

/**
 * 认证流模型：描述 realm 中可配置的认证执行链（别名、提供者、是否内置等）。
 *
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AuthenticationFlowModel implements Serializable {

    private String id;
    private String alias;
    private String description;
    private String providerId;
    private boolean topLevel;
    private boolean builtIn;

    /** @return 认证流内部 ID */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /** @return 认证流别名（唯一标识） */
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    /** @return 认证流描述 */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 认证流提供者 ID（如 basic-flow） */
    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    /** @return 是否为顶级认证流 */
    public boolean isTopLevel() {
        return topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    /** @return 是否为内置认证流 */
    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    /** 按别名排序 {@link AuthenticationFlowModel} 的比较器。 */
    public static class AuthenticationFlowComparator implements Comparator<AuthenticationFlowModel> {
        public static final AuthenticationFlowModel.AuthenticationFlowComparator SINGLETON =
                new AuthenticationFlowModel.AuthenticationFlowComparator();

        @Override
        public int compare(AuthenticationFlowModel left, AuthenticationFlowModel right) {
            //ensure consistent ordering of authenticationFlows.
            String l = left.getAlias() != null ? left.getAlias() : "\0";
            String r = right.getAlias() != null ? right.getAlias() : "\0";
            return l.compareTo(r);
        }
    }
}
