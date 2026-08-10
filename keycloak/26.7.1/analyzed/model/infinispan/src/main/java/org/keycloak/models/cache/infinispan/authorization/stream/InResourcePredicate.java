/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.keycloak.models.cache.infinispan.authorization.stream;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.keycloak.marshalling.Marshalling;
import org.keycloak.models.cache.infinispan.authorization.entities.InResource;
import org.keycloak.models.cache.infinispan.entities.Revisioned;

import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.infinispan.protostream.annotations.ProtoTypeId;

/**
 * 按资源 ID 过滤授权缓存条目的流式谓词。
 * <p>
 * 实现 {@link Predicate}，在批量失效时匹配实现了 {@link InResource} 的缓存实体。
 * 通过 ProtoStream 序列化以便在集群节点间传递查询条件。
 *
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
@ProtoTypeId(Marshalling.IN_RESOURCE_PREDICATE)
public class InResourcePredicate implements Predicate<Map.Entry<String, Revisioned>> {

    /** 待匹配的目标资源 ID。 */
    private final String resourceId;

    /** 私有构造，通过 {@link #create} 获取实例。 */
    private InResourcePredicate(String resourceId) {
        this.resourceId = Objects.requireNonNull(resourceId);
    }

    /** ProtoStream 工厂方法，创建按资源 ID 过滤的谓词。 */
    @ProtoFactory
    public static InResourcePredicate create(String resourceId) {
        return new InResourcePredicate(resourceId);
    }

    /** 返回待匹配的资源 ID（ProtoStream 序列化字段）。 */
    @ProtoField(1)
    String getResourceId() {
        return resourceId;
    }

    /** 判断缓存条目是否关联到指定资源。 */
    @Override
    public boolean test(Map.Entry<String, Revisioned> entry) {
        return entry.getValue() instanceof InResource inResource && resourceId.equals(inResource.getResourceId());
    }

}
