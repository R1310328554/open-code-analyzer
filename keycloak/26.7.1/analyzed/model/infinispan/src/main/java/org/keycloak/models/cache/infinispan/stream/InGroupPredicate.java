/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.models.cache.infinispan.stream;

import java.util.Map;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.entities.GroupNameQuery;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 缓存流过滤谓词：匹配 {@link GroupNameQuery} 且 groupId 相等的条目。
 */
@ProtoTypeId(Marshalling.IN_GROUP_PREDICATE)
public class InGroupPredicate implements Predicate<Map.Entry<String, Revisioned>> {
    /** 目标组 ID。 */
    private String group;

    /** 创建可链式配置的谓词实例。 */
    public static InGroupPredicate create() {
        return new InGroupPredicate();
    }

    /** 限定要匹配的组 ID。 */
    public InGroupPredicate group(String id) {
        group = id;
        return this;
    }

    @ProtoField(1)
    String getGroup() {
        return group;
    }

    void setGroup(String group) {
        this.group = group;
    }

    /** 判断缓存值是否为指定组的名称查询实体。 */
    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        return entry.getValue() instanceof GroupNameQuery groupNameQuery && group.equals(groupNameQuery.getGroupId());

    }

}