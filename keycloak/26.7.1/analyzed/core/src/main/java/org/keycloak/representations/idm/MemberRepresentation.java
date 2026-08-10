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

package org.keycloak.representations.idm;

/**
 * 组织（Organization）成员的 REST 表示，继承 {@link UserRepresentation} 并附加成员关系类型。
 */
public class MemberRepresentation extends UserRepresentation {

    /** 成员在组织中的关系类型（如正式成员、邀请中等）。 */
    private MembershipType membershipType;

    /** 无参构造。 */
    public MemberRepresentation() {
        super();
    }

    /**
     * 从已有用户表示复制字段。
     *
     * @param user 源用户表示
     */
    public MemberRepresentation(UserRepresentation user) {
        super(user);
    }

    /** @return 成员关系类型 */
    public MembershipType getMembershipType() {
        return membershipType;
    }

    /** @param membershipType 成员关系类型 */
    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType;
    }
}
