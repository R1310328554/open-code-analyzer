/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.representations.account;

import java.util.List;

/**
 * 用户对某客户端的授权同意记录，包含已授予的作用域列表及创建/更新时间戳。
 */
public class ConsentRepresentation {

    /** 用户已同意授予的作用域列表。 */
    private List<ConsentScopeRepresentation> grantedScopes;

    /** 同意记录创建时间（Unix 毫秒时间戳）。 */
    private Long createdDate;

    /** 同意记录最后更新时间（Unix 毫秒时间戳）。 */
    private Long lastUpdatedDate;

    /** 默认无参构造器。 */
    public ConsentRepresentation() {
    }

    /**
     * 构造包含全部字段的同意表示。
     *
     * @param grantedScopes 已授予作用域
     * @param createdDate 创建时间
     * @param lastUpdatedDate 最后更新时间
     */
    public ConsentRepresentation(List<ConsentScopeRepresentation> grantedScopes, Long createdDate, Long lastUpdatedDate) {
        this.grantedScopes = grantedScopes;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
    }

    /** @return 已授予的作用域列表 */
    public List<ConsentScopeRepresentation> getGrantedScopes() {
        return grantedScopes;
    }

    /** @param grantedScopes 已授予的作用域列表 */
    public void setGrantedScopes(List<ConsentScopeRepresentation> grantedScopes) {
        this.grantedScopes = grantedScopes;
    }

    /** @return 创建时间戳 */
    public Long getCreatedDate() {
        return createdDate;
    }

    /** @param createdDate 创建时间戳 */
    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    /** @return 最后更新时间戳 */
    public Long getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    /** @param lastUpdatedDate 最后更新时间戳 */
    public void setLastUpdatedDate(Long lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }
}
