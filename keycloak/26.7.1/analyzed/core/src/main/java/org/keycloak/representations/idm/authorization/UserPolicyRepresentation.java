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
package org.keycloak.representations.idm.authorization;

import java.util.HashSet;
import java.util.Set;

/**
 * 用户（user）类型授权策略的 REST 表示，按指定用户身份匹配请求。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class UserPolicyRepresentation extends AbstractPolicyRepresentation {

    /** 匹配的用户 ID 或用户名集合。 */
    private Set<String> users;

    /** @return 固定策略类型 {@code user} */
    @Override
    public String getType() {
        return "user";
    }

    /** @return 用户集合 */
    public Set<String> getUsers() {
        return users;
    }

    /** @param users 用户集合 */
    public void setUsers(Set<String> users) {
        this.users= users;
    }

    /** 添加单个用户。 */
    public void addUser(String name) {
        if (users == null) {
            users = new HashSet<>();
        }
        users.add(name);
    }
}
